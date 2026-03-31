package com.conor.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.conor.taskmanager.model.Task;
import com.conor.taskmanager.model.User;
import com.conor.taskmanager.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskReminderServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private NotificationSettingsService notificationSettingsService;

    @Mock
    private NtfyNotificationService ntfyNotificationService;

    private TaskReminderService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        service = new TaskReminderService(
                taskRepository, notificationSettingsService, ntfyNotificationService, 30);
        testUser = new User();
        testUser.setId(1L);
        testUser.setNtfyEnabled(true);
    }

    private Task createDueTask(int id) {
        Task task = new Task(id, "Task " + id, "Description", Task.Status.PENDING,
                Task.Priority.HIGH, LocalDateTime.now().plusMinutes(15));
        task.setUser(testUser);
        return task;
    }

    @Test
    void sendDueSoonReminders_sendsReminderAndMarksTask() {
        Task task = createDueTask(1);
        when(taskRepository.findByReminderSentAtIsNullAndDueDateBetweenAndStatusNot(
                any(), any(), eq(Task.Status.COMPLETED))).thenReturn(List.of(task));
        when(notificationSettingsService.canSendReminder(testUser)).thenReturn(true);

        service.sendDueSoonReminders();

        verify(ntfyNotificationService).sendTaskReminder(testUser, task);
        assertNotNull(task.getReminderSentAt());
        verify(taskRepository).save(task);
    }

    @Test
    void sendDueSoonReminders_skipsTaskWhenCannotSendReminder() {
        Task task = createDueTask(1);
        when(taskRepository.findByReminderSentAtIsNullAndDueDateBetweenAndStatusNot(
                any(), any(), eq(Task.Status.COMPLETED))).thenReturn(List.of(task));
        when(notificationSettingsService.canSendReminder(testUser)).thenReturn(false);

        service.sendDueSoonReminders();

        assertNull(task.getReminderSentAt());
    }
}
