package com.isums.econtractvnservice.domains.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class MultipartForwardRequest {

    @NotBlank
    private String path;

    @NotBlank
    private String method;

    private Map<String, String> headers;

    private Map<String, String> formFields;
}