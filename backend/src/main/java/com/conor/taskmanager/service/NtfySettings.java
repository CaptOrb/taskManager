package com.conor.taskmanager.service;

import com.conor.taskmanager.util.AppStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NtfySettings {
	private final String serverUrl;
	private final String publicUrl;
	private final long timeoutSeconds;
	private final String accessToken;
	private final boolean requireAccessToken;

	public NtfySettings(
			@Value("${notifications.ntfy.server-url:${NTFY_SERVER_URL:http://ntfy}}") String serverUrl,
			@Value("${notifications.ntfy.public-url:${NTFY_PUBLIC_URL:}}") String publicUrl,
			@Value("${notifications.ntfy.timeout-seconds:${NOTIFICATIONS_NTFY_TIMEOUT_SECONDS:10}}") long timeoutSeconds,
			@Value("${notifications.ntfy.access-token:${NTFY_ACCESS_TOKEN:}}") String accessToken,
			@Value("${notifications.ntfy.require-access-token:${NOTIFICATIONS_NTFY_REQUIRE_ACCESS_TOKEN:true}}") boolean requireAccessToken) {
		this.serverUrl = normalizeUrl(serverUrl);
		this.publicUrl = normalizeUrl(publicUrl);
		this.timeoutSeconds = timeoutSeconds;
		this.accessToken = AppStringUtils.trimToNull(accessToken);
		this.requireAccessToken = requireAccessToken;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public String getPublicUrlOrServerUrl() {
		if (publicUrl != null) {
			return publicUrl;
		}

		return serverUrl;
	}

	public long getTimeoutSeconds() {
		return timeoutSeconds;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public boolean isPublishAuthenticationConfigured() {
		return !requireAccessToken || accessToken != null;
	}

	private static String normalizeUrl(String rawUrl) {
		String normalizedUrl = AppStringUtils.trimToNull(rawUrl);
		if (normalizedUrl == null) {
			return null;
		}

		while (normalizedUrl.endsWith("/")) {
			normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
		}

		return normalizedUrl;
	}
}
