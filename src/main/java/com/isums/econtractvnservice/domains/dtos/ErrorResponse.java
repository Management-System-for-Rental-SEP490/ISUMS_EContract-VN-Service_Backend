package com.isums.econtractvnservice.domains.dtos;

import lombok.Data;

import java.time.Instant;

@Data
public class ErrorResponse {
    private final String error;
    private final String message;
    private final Instant time = Instant.now();
}