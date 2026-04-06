package com.healthcare.erp.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, List<String>> fieldErrors) {

    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, LocalDateTime.now(), null);
    }

    public static ErrorResponse ofValidation(int status, String message,
                                              Map<String, List<String>> fieldErrors) {
        return new ErrorResponse(status, message, LocalDateTime.now(), fieldErrors);
    }
}
