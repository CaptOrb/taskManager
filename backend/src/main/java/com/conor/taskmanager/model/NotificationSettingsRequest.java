package com.conor.taskmanager.model;

import jakarta.validation.constraints.Size;

public class NotificationSettingsRequest {
	private boolean enabled;

	@Size(max = 128, message = "Topic can only be 128 characters")
	private String topic;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
