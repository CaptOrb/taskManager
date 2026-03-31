package com.conor.taskmanager.model;

public class NotificationSettingsResponse {
	private boolean enabled;
	private String publicUrl;
	private String topicPrefix;
	private String topic;
	private int reminderMinutesBeforeDue;

	public NotificationSettingsResponse(
			boolean enabled,
			String publicUrl,
			String topicPrefix,
			String topic,
			int reminderMinutesBeforeDue) {
		this.enabled = enabled;
		this.publicUrl = publicUrl;
		this.topicPrefix = topicPrefix;
		this.topic = topic;
		this.reminderMinutesBeforeDue = reminderMinutesBeforeDue;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getPublicUrl() {
		return publicUrl;
	}

	public String getTopicPrefix() {
		return topicPrefix;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public int getReminderMinutesBeforeDue() {
		return reminderMinutesBeforeDue;
	}

	public void setReminderMinutesBeforeDue(int reminderMinutesBeforeDue) {
		this.reminderMinutesBeforeDue = reminderMinutesBeforeDue;
	}
}
