package com.conor.taskmanager.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ValidationException extends RuntimeException {

    private final List<String> errors;
    private final Map<String, List<String>> fieldErrors;

    public ValidationException(String message) {
        super(message);
        this.errors = List.of(message);
        this.fieldErrors = Map.of();
    }

    public ValidationException(List<String> errors) {
        super(errors == null || errors.isEmpty() ? "Validation failed" : errors.get(0));
        this.errors = errors == null ? List.of() : List.copyOf(errors);
        this.fieldErrors = Map.of();
    }

    public ValidationException(Map<String, List<String>> fieldErrors) {
        super("Validation failed");
        this.fieldErrors = toOrderedFieldErrors(fieldErrors);
        this.errors = this.fieldErrors.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public List<String> getErrors() {
        return errors;
    }

    public Map<String, List<String>> getFieldErrors() {
        return fieldErrors;
    }

    private static Map<String, List<String>> toOrderedFieldErrors(Map<String, List<String>> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return Map.of();
        }

        Map<String, List<String>> orderedFieldErrors = new LinkedHashMap<>();
        fieldErrors.forEach((field, messages) -> orderedFieldErrors.put(field, List.copyOf(messages)));
        return Collections.unmodifiableMap(orderedFieldErrors);
    }
}
