package com.conor.taskmanager.service;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.NtfyAuthenticationException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.NotificationSettingsRequest;
import com.conor.taskmanager.model.NotificationSettingsResponse;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.util.AppStringUtils;
import com.conor.taskmanager.util.FieldErrorUtils;

@Service
public class NotificationSettingsService {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Pattern TOPIC_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
	private static final String TOPIC_SUGGESTION_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
	private static final int TOPIC_SUGGESTION_LENGTH = 10;

	private final UserLookupService userLookupService;
	private final NtfyNotificationService ntfyNotificationService;
	private final NtfySettings ntfySettings;
	private final NtfyTopicResolver ntfyTopicResolver;
	private final int reminderMinutesBeforeDue;

	public NotificationSettingsService(
			UserLookupService userLookupService,
			NtfyNotificationService ntfyNotificationService,
			NtfySettings ntfySettings,
			NtfyTopicResolver ntfyTopicResolver,
			@Value("${notifications.reminder.minutes-before-due:30}") int reminderMinutesBeforeDue) {
		this.userLookupService = userLookupService;
		this.ntfyNotificationService = ntfyNotificationService;
		this.ntfySettings = ntfySettings;
		this.ntfyTopicResolver = ntfyTopicResolver;
		this.reminderMinutesBeforeDue = reminderMinutesBeforeDue;
	}

	@Transactional(readOnly = true)
	public NotificationSettingsResponse getSettings(Long userId) {
		User user = userLookupService.getUserById(userId);
		return mapToResponse(user);
	}

	@Transactional
	public NotificationSettingsResponse updateSettings(Long userId, NotificationSettingsRequest request) {
		User user = userLookupService.getUserById(userId);

		String normalizedTopic = AppStringUtils.trimToNull(request.getTopic());

		Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

		if (request.isEnabled() && normalizedTopic == null) {
			FieldErrorUtils.addFieldError(fieldErrors, "topic", "Topic is required when notifications are enabled");
		}

		if (request.isEnabled() && !ntfySettings.isPublishAuthenticationConfigured()) {
			addPublishAuthenticationConfigurationError(fieldErrors);
		}

		if (normalizedTopic != null && !TOPIC_PATTERN.matcher(normalizedTopic).matches()) {
			FieldErrorUtils.addFieldError(fieldErrors, "topic", "Topic can only contain letters, numbers, _ and -");
		}

		if (!fieldErrors.isEmpty()) {
			throw new ValidationException(fieldErrors);
		}

		user.setNtfyEnabled(request.isEnabled());
		user.setNtfyTopic(normalizedTopic);

		return mapToResponse(user);
	}

	@Transactional(readOnly = true)
	public void sendTestNotification(Long userId) {
		User user = userLookupService.getUserById(userId);

		Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
		if (ntfySettings.getServerUrl() == null) {
			FieldErrorUtils.addFieldError(
					fieldErrors,
					"serverUrl",
					"Configure notifications.ntfy.server-url (or NTFY_SERVER_URL) before sending a test notification");
		}
		if (ntfyTopicResolver.resolvePublishTopic(user) == null) {
			FieldErrorUtils.addFieldError(fieldErrors, "topic", "Configure a topic before sending a test notification");
		}
		if (!ntfySettings.isPublishAuthenticationConfigured()) {
			addPublishAuthenticationConfigurationError(fieldErrors);
		}

		if (!fieldErrors.isEmpty()) {
			throw new ValidationException(fieldErrors);
		}

		try {
			ntfyNotificationService.sendTestNotification(user);
		} catch (NtfyAuthenticationException ntfyAuthenticationException) {
			Map<String, List<String>> publishAuthErrors = new LinkedHashMap<>();
			throw new ValidationException(publishAuthErrors);
		}
	}

	public boolean canSendReminder(User user) {
		return user.isNtfyEnabled()
				&& ntfySettings.getServerUrl() != null
				&& ntfySettings.isPublishAuthenticationConfigured()
				&& ntfyTopicResolver.resolvePublishTopic(user) != null;
	}

	public String generateTopicSuggestion() {
		StringBuilder builder = new StringBuilder(TOPIC_SUGGESTION_LENGTH);
		for (int i = 0; i < TOPIC_SUGGESTION_LENGTH; i++) {
			int index = SECURE_RANDOM.nextInt(TOPIC_SUGGESTION_ALPHABET.length());
			builder.append(TOPIC_SUGGESTION_ALPHABET.charAt(index));
		}
		return builder.toString();
	}

	private NotificationSettingsResponse mapToResponse(User user) {
		return new NotificationSettingsResponse(
				user.isNtfyEnabled(),
				ntfySettings.getPublicUrlOrServerUrl(),
				ntfyTopicResolver.getTopicPrefix(user),
				user.getNtfyTopic(),
				reminderMinutesBeforeDue);
	}

	private static void addPublishAuthenticationConfigurationError(Map<String, List<String>> fieldErrors) {
		FieldErrorUtils.addFieldError(
				fieldErrors,
				"configuration",
				"Configure notifications.ntfy.access-token (or NTFY_ACCESS_TOKEN) before enabling ntfy notifications");
	}
}
