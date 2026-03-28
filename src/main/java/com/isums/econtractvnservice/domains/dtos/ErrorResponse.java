package com.isums.econtractvnservice.domains.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private Instant time;

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.time = Instant.now();
    }
}