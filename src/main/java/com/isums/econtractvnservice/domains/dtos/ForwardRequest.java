package com.isums.econtractvnservice.domains.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ForwardRequest {

    @NotBlank
    private String path;

    @NotBlank
    private String method;

    private String body;

    private Map<String, String> headers;
}