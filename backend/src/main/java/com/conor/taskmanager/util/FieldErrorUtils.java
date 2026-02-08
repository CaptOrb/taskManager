package com.conor.taskmanager.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class FieldErrorUtils {

	private FieldErrorUtils() {
	}

	public static void addFieldError(Map<String, List<String>> fieldErrors, String field, String message) {
		fieldErrors.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
	}
}
