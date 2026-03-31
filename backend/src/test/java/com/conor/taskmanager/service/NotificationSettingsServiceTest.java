package com.conor.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.conor.taskmanager.exception.ValidationException;
import com.conor.taskmanager.model.NotificationSettingsRequest;
import com.conor.taskmanager.model.NotificationSettingsResponse;
import com.conor.taskmanager.model.User;

@ExtendWith(MockitoExtension.class)
public class NotificationSettingsServiceTest {

    @Mock
    private UserLookupService userLookupService;

    @Mock
    private NtfyNotificationService ntfyNotificationService;

    @Mock
    private NtfySettings ntfySettings;

    @Mock
    private NtfyTopicResolver ntfyTopicResolver;

    private NotificationSettingsService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        service = new NotificationSettingsService(
                userLookupService, ntfyNotificationService, ntfySettings, ntfyTopicResolver, 30);
        testUser = new User();
        testUser.setId(1L);
        testUser.setNtfyTopic("my-topic");
        testUser.setNtfyEnabled(false);
    }

    private void stubUserLookup() {
        when(userLookupService.getUserById(1L)).thenReturn(testUser);
    }

    @Test
    void updateSettings_enableWithValidTopic_enablesNotifications() {
        stubUserLookup();
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(true);
        when(ntfySettings.getPublicUrlOrServerUrl()).thenReturn("https://ntfy.example.com");
        when(ntfyTopicResolver.getTopicPrefix(testUser)).thenReturn("tm-1-");

        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setEnabled(true);
        request.setTopic("valid-topic");

        NotificationSettingsResponse response = service.updateSettings(1L, request);

        assertTrue(response.isEnabled());
        assertTrue(testUser.isNtfyEnabled());
        assertEquals("valid-topic", testUser.getNtfyTopic());
    }

    @Test
    void updateSettings_enableWithNoTopic_throwsValidation() {
        stubUserLookup();
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(true);

        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setEnabled(true);
        request.setTopic(null);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateSettings(1L, request));
        assertTrue(ex.getFieldErrors().containsKey("topic"));
    }

    @Test
    void updateSettings_enableWithNoAuthConfig_throwsValidation() {
        stubUserLookup();
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(false);

        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setEnabled(true);
        request.setTopic("valid-topic");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateSettings(1L, request));
        assertTrue(ex.getFieldErrors().containsKey("configuration"));
    }

    @Test
    void updateSettings_invalidTopicFormat_throwsValidation() {
        stubUserLookup();
        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setEnabled(false);
        request.setTopic("invalid topic!");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.updateSettings(1L, request));
        assertTrue(ex.getFieldErrors().containsKey("topic"));
    }

    @Test
    void updateSettings_disableWithNullTopic_clearsTopicAndDisables() {
        stubUserLookup();
        when(ntfySettings.getPublicUrlOrServerUrl()).thenReturn("https://ntfy.example.com");
        when(ntfyTopicResolver.getTopicPrefix(testUser)).thenReturn("tm-1-");

        testUser.setNtfyEnabled(true);
        testUser.setNtfyTopic("old-topic");

        NotificationSettingsRequest request = new NotificationSettingsRequest();
        request.setEnabled(false);
        request.setTopic(null);

        NotificationSettingsResponse response = service.updateSettings(1L, request);

        assertFalse(response.isEnabled());
        assertFalse(testUser.isNtfyEnabled());
        assertNull(testUser.getNtfyTopic());
    }

    @Test
    void sendTestNotification_missingServerUrl_throwsValidation() {
        stubUserLookup();
        when(ntfySettings.getServerUrl()).thenReturn(null);
        when(ntfyTopicResolver.resolvePublishTopic(testUser)).thenReturn("tm-1-topic");
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.sendTestNotification(1L));
        assertTrue(ex.getFieldErrors().containsKey("serverUrl"));
    }

    @Test
    void sendTestNotification_missingTopic_throwsValidation() {
        stubUserLookup();
        when(ntfySettings.getServerUrl()).thenReturn("https://ntfy.example.com");
        when(ntfyTopicResolver.resolvePublishTopic(testUser)).thenReturn(null);
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.sendTestNotification(1L));
        assertTrue(ex.getFieldErrors().containsKey("topic"));
    }

    @Test
    void canSendReminder_allConditionsMet_returnsTrue() {
        testUser.setNtfyEnabled(true);
        when(ntfySettings.getServerUrl()).thenReturn("https://ntfy.example.com");
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(true);
        when(ntfyTopicResolver.resolvePublishTopic(testUser)).thenReturn("tm-1-topic");

        assertTrue(service.canSendReminder(testUser));
    }

    @Test
    void canSendReminder_notificationsDisabled_returnsFalse() {
        testUser.setNtfyEnabled(false);

        assertFalse(service.canSendReminder(testUser));
    }

    @Test
    void canSendReminder_noServerUrl_returnsFalse() {
        testUser.setNtfyEnabled(true);
        when(ntfySettings.getServerUrl()).thenReturn(null);

        assertFalse(service.canSendReminder(testUser));
    }

    @Test
    void canSendReminder_noAuthConfig_returnsFalse() {
        testUser.setNtfyEnabled(true);
        when(ntfySettings.getServerUrl()).thenReturn("https://ntfy.example.com");
        when(ntfySettings.isPublishAuthenticationConfigured()).thenReturn(false);

        assertFalse(service.canSendReminder(testUser));
    }
}
