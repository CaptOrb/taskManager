package com.conor.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.conor.taskmanager.model.Task;

public class NtfyNotificationServiceTest {

    @Test
    void mapTaskPriorityToNtfyPriority_high_returnsHigh() {
        assertEquals("high", NtfyNotificationService.mapTaskPriorityToNtfyPriority(Task.Priority.HIGH));
    }

    @Test
    void mapTaskPriorityToNtfyPriority_low_returnsLow() {
        assertEquals("low", NtfyNotificationService.mapTaskPriorityToNtfyPriority(Task.Priority.LOW));
    }

    @Test
    void mapTaskPriorityToNtfyPriority_medium_returnsDefault() {
        assertEquals("default", NtfyNotificationService.mapTaskPriorityToNtfyPriority(Task.Priority.MEDIUM));
    }

    @Test
    void buildTaskClickUrl_withValidInputs_returnsUrl() {
        assertEquals("https://example.com/tasks/42",
                NtfyNotificationService.buildTaskClickUrl("https://example.com", 42));
    }

    @Test
    void buildTaskClickUrl_stripsTrailingSlashes() {
        assertEquals("https://example.com/tasks/1",
                NtfyNotificationService.buildTaskClickUrl("https://example.com///", 1));
    }

    @Test
    void buildTaskClickUrl_nullTaskId_returnsNull() {
        assertNull(NtfyNotificationService.buildTaskClickUrl("https://example.com", null));
    }

    @Test
    void buildTaskClickUrl_nullBaseUrl_returnsNull() {
        assertNull(NtfyNotificationService.buildTaskClickUrl(null, 1));
    }

    @Test
    void buildTaskClickUrl_blankBaseUrl_returnsNull() {
        assertNull(NtfyNotificationService.buildTaskClickUrl("   ", 1));
    }

    @Test
    void buildTaskViewAction_withValidUrl_returnsAction() {
        assertEquals("view, Open task, https://example.com/tasks/1",
                NtfyNotificationService.buildTaskViewAction("https://example.com/tasks/1"));
    }

    @Test
    void buildTaskViewAction_nullUrl_returnsNull() {
        assertNull(NtfyNotificationService.buildTaskViewAction(null));
    }

    @Test
    void buildTaskViewAction_blankUrl_returnsNull() {
        assertNull(NtfyNotificationService.buildTaskViewAction("   "));
    }
}
