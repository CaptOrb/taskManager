package com.conor.taskmanager.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.conor.taskmanager.exception.UserNotFoundException;
import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.NotificationSettingsRequest;
import com.conor.taskmanager.model.NotificationSettingsResponse;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.UserRepository;

@Service
public class NotificationSettingsService {
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final Pattern TOPIC_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,128}$");
	private static final String TOPIC_SUGGESTION_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-";
	private static final int TOPIC_SUGGESTION_LENGTH = 10;

	private final UserRepository userRepository;
	private final NtfyNotificationService ntfyNotificationService;
	private final NtfySettings ntfySettings;
	private final NtfyTopicResolver ntfyTopicResolver;
	private final int reminderMinutesBeforeDue;

	public NotificationSettingsService(
			UserRepository userRepository,
			NtfyNotificationService ntfyNotificationService,
			NtfySettings ntfySettings,
			NtfyTopicResolver ntfyTopicResolver,
			@Value("${notifications.reminder.minutes-before-due:30}") int reminderMinutesBeforeDue) {
		this.userRepository = userRepository;
		this.ntfyNotificationService = ntfyNotificationService;
		this.ntfySettings = ntfySettings;
		this.ntfyTopicResolver = ntfyTopicResolver;
		this.reminderMinutesBeforeDue = reminderMinutesBeforeDue;
	}

	@Transactional(readOnly = true)
	public NotificationSettingsResponse getSettings(String username) {
		User user = getUserByUsername(username);
		return mapToResponse(user);
	}

	@Transactional
	public NotificationSettingsResponse updateSettings(String username, NotificationSettingsRequest request) {
		User user = getUserByUsername(username);

		String normalizedTopic = trimToNull(request.getTopic());

		Map<String, List<String>> fieldErrors = new LinkedHashMap<>();

		if (request.isEnabled() && normalizedTopic == null) {
			addFieldError(fieldErrors, "topic", "Topic is required when notifications are enabled");
		}

		if (normalizedTopic != null && !TOPIC_PATTERN.matcher(normalizedTopic).matches()) {
			addFieldError(fieldErrors, "topic", "Topic can only contain letters, numbers, _ and -");
		}

		if (!fieldErrors.isEmpty()) {
			throw new ValidationException(fieldErrors);
		}

		user.setNtfyEnabled(request.isEnabled());
		user.setNtfyTopic(normalizedTopic);

		User savedUser = userRepository.save(user);
		return mapToResponse(savedUser);
	}

	@Transactional(readOnly = true)
	public void sendTestNotification(String username) {
		User user = getUserByUsername(username);

		Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
		if (ntfySettings.getServerUrl() == null) {
			addFieldError(
					fieldErrors,
					"serverUrl",
					"Configure notifications.ntfy.server-url (or NTFY_SERVER_URL) before sending a test notification");
		}
		if (ntfyTopicResolver.resolvePublishTopic(user) == null) {
			addFieldError(fieldErrors, "topic", "Configure a topic before sending a test notification");
		}

		if (!fieldErrors.isEmpty()) {
			throw new ValidationException(fieldErrors);
		}

		ntfyNotificationService.sendTestNotification(user);
	}

	public boolean canSendReminder(User user) {
		return user.isNtfyEnabled()
				&& ntfySettings.getServerUrl() != null
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

	private User getUserByUsername(String username) {
		return userRepository.findByUserName(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static void addFieldError(Map<String, List<String>> fieldErrors, String field, String message) {
		fieldErrors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
	}
}
