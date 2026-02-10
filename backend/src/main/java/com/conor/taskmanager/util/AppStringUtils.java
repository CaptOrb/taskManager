package com.conor.taskmanager.util;

public final class AppStringUtils {

	private AppStringUtils() {
	}

	public static String trimToNull(String value) {
		if (value == null) {
			return null;
		}

		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
