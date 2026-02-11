package com.conor.taskmanager.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationSettingsService notificationSettingsService;

	public NotificationController(NotificationSettingsService notificationSettingsService) {
		this.notificationSettingsService = notificationSettingsService;
	}

	@GetMapping("/settings")
	public ResponseEntity<NotificationSettingsResponse> getSettings(@AuthenticationPrincipal UserDetails userDetails) {
		NotificationSettingsResponse settings = notificationSettingsService.getSettings(userDetails.getUsername());
		return ResponseEntity.ok(settings);
	}

	@PutMapping("/settings")
	public ResponseEntity<NotificationSettingsResponse> updateSettings(
			@Valid @RequestBody NotificationSettingsRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		NotificationSettingsResponse settings = notificationSettingsService.updateSettings(userDetails.getUsername(), request);
		return ResponseEntity.ok(settings);
	}

	@PostMapping("/test")
	public ResponseEntity<Map<String, String>> sendTestNotification(@AuthenticationPrincipal UserDetails userDetails) {
		notificationSettingsService.sendTestNotification(userDetails.getUsername());
		return ResponseEntity.ok(Collections.singletonMap("message", "Test notification sent"));
	}

	@GetMapping("/topic-suggestion")
	public ResponseEntity<Map<String, String>> getTopicSuggestion() {
		String suggestion = notificationSettingsService.generateTopicSuggestion();
		return ResponseEntity.ok(Collections.singletonMap("topic", suggestion));
	}
}
