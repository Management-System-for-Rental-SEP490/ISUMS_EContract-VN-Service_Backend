package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.clients.VnptHttpClient;
import com.isums.econtractvnservice.domains.dtos.ErrorResponse;
import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import com.isums.econtractvnservice.exceptions.BadRequestException;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Slf4j
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
            ResponseEntity<String> response = vnptHttpClient.forward(
                    request.getPath(),
                    method,
                    request.getBody(),
                    request.getHeaders()
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            if (response.getHeaders().getContentType() != null) {
                responseHeaders.setContentType(response.getHeaders().getContentType());
            }

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (RestClientResponseException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null && ex.getResponseHeaders().getContentType() != null) {
                responseHeaders.setContentType(ex.getResponseHeaders().getContentType());
            }

            return new ResponseEntity<>(ex.getResponseBodyAsString(), responseHeaders, ex.getStatusCode());

        } catch (Exception ex) {
            log.error("VNPT forward failed. path={}, method={}", request.getPath(), request.getMethod(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Gateway error", ex.getClass().getName() + ": " + ex.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> forwardMultipart(MultipartForwardRequest request, MultipartFile file) {
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
            ResponseEntity<String> response = vnptHttpClient.forwardMultipart(
                    request.getPath(),
                    method,
                    request.getHeaders(),
                    request.getFormFields(),
                    file
            );

            HttpHeaders responseHeaders = new HttpHeaders();
            if (response.getHeaders().getContentType() != null) {
                responseHeaders.setContentType(response.getHeaders().getContentType());
            }

            return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());

        } catch (RestClientResponseException ex) {
            HttpHeaders responseHeaders = new HttpHeaders();
            if (ex.getResponseHeaders() != null && ex.getResponseHeaders().getContentType() != null) {
                responseHeaders.setContentType(ex.getResponseHeaders().getContentType());
            }

            return new ResponseEntity<>(ex.getResponseBodyAsString(), responseHeaders, ex.getStatusCode());

        } catch (Exception ex) {
            log.error("VNPT multipart forward failed. path={}, method={}", request.getPath(), request.getMethod(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ErrorResponse("Gateway error", ex.getClass().getName() + ": " + ex.getMessage()));
        }
    }
}