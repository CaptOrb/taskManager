package com.conor.taskmanager.exception;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(String message, List<String> errors, Map<String, List<String>> fieldErrors) {

    public static ApiErrorResponse single(String message) {
        return new ApiErrorResponse(message, null, null);
    }

    public static ApiErrorResponse validation(List<String> messages) {
        return validation(messages, null);
    }

    public static ApiErrorResponse validation(List<String> messages, Map<String, List<String>> fieldErrors) {
        return new ApiErrorResponse("Validation failed", messages, fieldErrors);
    }
}
