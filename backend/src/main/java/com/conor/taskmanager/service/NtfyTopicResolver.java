package com.conor.taskmanager.service;

import com.conor.taskmanager.util.AppStringUtils;
import org.springframework.stereotype.Component;

import com.conor.taskmanager.model.User;

@Component
public class NtfyTopicResolver {
	private static final String TOPIC_PREFIX = "tm";

	public String resolvePublishTopic(User user) {
		if (user == null) {
			return null;
		}

		String rawTopic = AppStringUtils.trimToNull(user.getNtfyTopic());
		String userTopicPrefix = getTopicPrefix(user);
		if (rawTopic == null || userTopicPrefix == null) {
			return null;
		}

		return userTopicPrefix + rawTopic;
	}

	public String getTopicPrefix(User user) {
		if (user == null || user.getId() == null) {
			return null;
		}

		return TOPIC_PREFIX + "-" + user.getId() + "-";
	}
}
