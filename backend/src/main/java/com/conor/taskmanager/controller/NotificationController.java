package com.conor.taskmanager.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.conor.taskmanager.model.NotificationSettingsRequest;
import com.conor.taskmanager.model.NotificationSettingsResponse;
import com.conor.taskmanager.service.NotificationSettingsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationSettingsService notificationSettingsService;

	@GetMapping("/settings")
	public ResponseEntity<NotificationSettingsResponse> getSettings() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		NotificationSettingsResponse settings = notificationSettingsService.getSettings(username);
		return ResponseEntity.ok(settings);
	}

	@PutMapping("/settings")
	public ResponseEntity<NotificationSettingsResponse> updateSettings(
			@Valid @RequestBody NotificationSettingsRequest request) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		NotificationSettingsResponse settings = notificationSettingsService.updateSettings(username, request);
		return ResponseEntity.ok(settings);
	}

	@PostMapping("/test")
	public ResponseEntity<Map<String, String>> sendTestNotification() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		notificationSettingsService.sendTestNotification(username);
		return ResponseEntity.ok(Collections.singletonMap("message", "Test notification sent"));
	}
}
