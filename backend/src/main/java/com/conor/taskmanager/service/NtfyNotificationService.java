package com.conor.taskmanager.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.conor.taskmanager.exception.NtfyAuthenticationException;
import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;

@Service
public class NtfyNotificationService {
	private static final Logger logger = LoggerFactory.getLogger(NtfyNotificationService.class);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final NtfySettings ntfySettings;
	private final NtfyTopicResolver ntfyTopicResolver;

	@Value("${notifications.reminder.click-base-url:${APP_BASE_URL:}}")
	private String reminderClickBaseUrl;

	public NtfyNotificationService(NtfySettings ntfySettings, NtfyTopicResolver ntfyTopicResolver) {
		this.ntfySettings = ntfySettings;
		this.ntfyTopicResolver = ntfyTopicResolver;
	}

	public void sendTaskReminder(User user, Task task) {
		String title = "Task due soon";
		String message = "%s\nDue: %s".formatted(task.getTitle(), task.getDueDate().format(DATE_FORMATTER));
		String priority = mapTaskPriorityToNtfyPriority(task.getPriority());
		String clickUrl = buildTaskClickUrl(reminderClickBaseUrl, task.getId());
		sendNotification(user, title, message, "alarm_clock", priority, clickUrl);
	}

	public void sendTestNotification(User user) {
		sendNotification(
				user,
				"Task Manager notification test",
				"ntfy is configured and working.",
				"white_check_mark",
				"default",
				null);
	}

	static String mapTaskPriorityToNtfyPriority(Task.Priority priority) {
		if (priority == Task.Priority.HIGH) {
			return "high";
		}

		if (priority == Task.Priority.LOW) {
			return "low";
		}

		return "default";
	}

	static String buildTaskClickUrl(String clickBaseUrl, Integer taskId) {
		if (taskId == null || clickBaseUrl == null) {
			return null;
		}

		String normalizedBaseUrl = clickBaseUrl.trim();
		if (normalizedBaseUrl.isEmpty()) {
			return null;
		}

		if (!normalizedBaseUrl.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
			normalizedBaseUrl = "https://" + normalizedBaseUrl;
		}

		while (normalizedBaseUrl.endsWith("/")) {
			normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
		}

		return normalizedBaseUrl + "/tasks/" + taskId;
	}

	static String buildTaskViewAction(String clickUrl) {
		if (clickUrl == null) {
			return null;
		}

		String normalizedClickUrl = clickUrl.trim();
		if (normalizedClickUrl.isEmpty()) {
			return null;
		}

		return "view, Open task, " + normalizedClickUrl;
	}

	private void sendNotification(
			User user,
			String title,
			String body,
			String tags,
			String priority,
			String clickUrl) {
		String topic = ntfyTopicResolver.resolvePublishTopic(user);
		String serverUrl = ntfySettings.getServerUrl();
		String accessToken = ntfySettings.getAccessToken();

		if (!ntfySettings.isPublishAuthenticationConfigured()) {
			throw new IllegalStateException(
					"ntfy publish authentication is required; configure notifications.ntfy.access-token (or NTFY_ACCESS_TOKEN)");
		}

		if (topic == null || serverUrl == null) {
			throw new IllegalStateException("ntfy topic and server URL are required");
		}

		String encodedTopic = URLEncoder.encode(topic, StandardCharsets.UTF_8).replace("+", "%20");
		URI publishUri = URI.create(serverUrl + "/" + encodedTopic);

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(publishUri)
				.timeout(Duration.ofSeconds(ntfySettings.getTimeoutSeconds()))
				.header("Title", title)
				.header("Tags", tags)
				.header("Priority", priority)
				.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));

		if (accessToken != null) {
			requestBuilder.header("Authorization", "Bearer " + accessToken);
		}

		if (clickUrl != null && !clickUrl.isBlank()) {
			requestBuilder.header("Actions", buildTaskViewAction(clickUrl));
		}

		try {
			HttpResponse<String> response = httpClient.send(
					requestBuilder.build(),
					HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

			if (response.statusCode() >= 400) {
				if (response.statusCode() == 401 || response.statusCode() == 403) {
					throw new NtfyAuthenticationException(
							"ntfy rejected publish credentials with status %d".formatted(response.statusCode()));
				}

				throw new IllegalStateException(
						"ntfy request failed with status %d".formatted(response.statusCode()));
			}
		} catch (NtfyAuthenticationException ntfyAuthenticationException) {
			logger.warn("Failed to send ntfy notification: {}", ntfyAuthenticationException.getMessage());
			throw ntfyAuthenticationException;
		} catch (InterruptedException interruptedException) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("ntfy request interrupted", interruptedException);
		} catch (Exception exception) {
			logger.warn("Failed to send ntfy notification", exception);
			throw new IllegalStateException("Failed to send ntfy notification", exception);
		}
	}
}
