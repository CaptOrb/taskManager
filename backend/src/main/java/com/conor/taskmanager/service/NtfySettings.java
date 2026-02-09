package com.conor.taskmanager.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NtfySettings {
	private final String serverUrl;
	private final String publicUrl;
	private final long timeoutSeconds;
	private final String accessToken;

	public NtfySettings(
			@Value("${notifications.ntfy.server-url:${NTFY_SERVER_URL:http://ntfy}}") String serverUrl,
			@Value("${notifications.ntfy.public-url:${NTFY_PUBLIC_URL:}}") String publicUrl,
			@Value("${notifications.ntfy.timeout-seconds:${NOTIFICATIONS_NTFY_TIMEOUT_SECONDS:10}}") long timeoutSeconds,
			@Value("${notifications.ntfy.access-token:${NTFY_ACCESS_TOKEN:}}") String accessToken) {
		this.serverUrl = normalizeUrl(serverUrl);
		this.publicUrl = normalizeUrl(publicUrl);
		this.timeoutSeconds = timeoutSeconds;
		this.accessToken = trimToNull(accessToken);
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

	private static String normalizeUrl(String rawUrl) {
		String normalizedUrl = trimToNull(rawUrl);
		if (normalizedUrl == null) {
			return null;
		}

		while (normalizedUrl.endsWith("/")) {
			normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
		}

		return normalizedUrl;
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
