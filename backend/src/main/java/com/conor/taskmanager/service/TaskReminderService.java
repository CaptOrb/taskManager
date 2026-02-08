package com.conor.taskmanager.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;

@Service
public class TaskReminderService {
	private static final Logger logger = LoggerFactory.getLogger(TaskReminderService.class);

	private final TaskRepository taskRepository;
	private final NotificationSettingsService notificationSettingsService;
	private final NtfyNotificationService ntfyNotificationService;
	private final int reminderMinutesBeforeDue;

	public TaskReminderService(
			TaskRepository taskRepository,
			NotificationSettingsService notificationSettingsService,
			NtfyNotificationService ntfyNotificationService,
			@Value("${notifications.reminder.minutes-before-due:30}") int reminderMinutesBeforeDue) {
		this.taskRepository = taskRepository;
		this.notificationSettingsService = notificationSettingsService;
		this.ntfyNotificationService = ntfyNotificationService;
		this.reminderMinutesBeforeDue = reminderMinutesBeforeDue;
	}

	@Scheduled(fixedDelayString = "${notifications.reminder.poll-interval-ms:60000}")
	public void sendDueSoonReminders() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowEnd = now.plusMinutes(reminderMinutesBeforeDue);

		List<Task> tasksDueSoon = taskRepository.findByReminderSentAtIsNullAndDueDateBetweenAndStatusNot(
				now,
				windowEnd,
				Task.Status.COMPLETED);

		for (Task task : tasksDueSoon) {
			User user = task.getUser();
			if (!notificationSettingsService.canSendReminder(user)) {
				continue;
			}

			try {
				ntfyNotificationService.sendTaskReminder(user, task);
				task.setReminderSentAt(LocalDateTime.now());
				taskRepository.save(task);
			} catch (Exception exception) {
				logger.warn("Failed to send reminder for task {}", task.getId(), exception);
			}
		}
	}
}
