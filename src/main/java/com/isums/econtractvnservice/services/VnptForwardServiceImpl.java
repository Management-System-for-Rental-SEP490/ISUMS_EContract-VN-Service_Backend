package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.clients.VnptHttpClient;
import com.isums.econtractvnservice.domains.dtos.ErrorResponse;
import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.exceptions.BadRequestException;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VnptForwardServiceImpl implements VnptForwardService {

    private final VnptHttpClient vnptHttpClient;

    @Override
    public ResponseEntity<?> forward(ForwardRequest request) {
        if (!StringUtils.hasText(request.getPath()) || !StringUtils.hasText(request.getMethod())) {
            throw new BadRequestException("Path and method are required");
        }

        HttpMethod method;
        try {
            method = HttpMethod.valueOf(request.getMethod().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid method");
        }

        try {
            return vnptHttpClient.forward(
                    request.getPath(),
                    method,
                    request.getBody(),
                    request.getHeaders()
            );
        } catch (RestClientResponseException ex) {
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders() == null ? new org.springframework.http.HttpHeaders() : ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    new ErrorResponse("Gateway error", ex.getMessage())
            );
        }
    }
}
