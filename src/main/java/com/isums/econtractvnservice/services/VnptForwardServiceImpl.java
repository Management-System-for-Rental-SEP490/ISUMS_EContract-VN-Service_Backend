package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.clients.VnptHttpClient;
import com.isums.econtractvnservice.domains.dtos.ErrorResponse;
import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import com.isums.econtractvnservice.exceptions.BadRequestException;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnptForwardServiceImpl implements VnptForwardService {

    private static final Set<String> ALLOWED_METHODS =
            Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS");

    private final VnptHttpClient vnptHttpClient;

    private HttpMethod parseMethod(String raw) {
        String upper = raw.toUpperCase(Locale.ROOT);
        if (!ALLOWED_METHODS.contains(upper)) {
            throw new BadRequestException("Invalid method");
        }
        return HttpMethod.valueOf(upper);
    }

    @Override
    public ResponseEntity<?> forward(ForwardRequest request) {
        if (!StringUtils.hasText(request.getPath()) || !StringUtils.hasText(request.getMethod())) {
            throw new BadRequestException("Path and method are required");
        }

        HttpMethod method = parseMethod(request.getMethod());

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

        HttpMethod method = parseMethod(request.getMethod());

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

    @Override
    public ResponseEntity<byte[]> forwardBinary(ForwardRequest request) {
        if (!StringUtils.hasText(request.getPath())) {
            throw new BadRequestException("path (downloadUrl) is required");
        }

        try {
            ResponseEntity<byte[]> response = vnptHttpClient.forwardBinary(
                    request.getPath(),
                    request.getHeaders()
            );

            if (response.getBody() == null || response.getBody().length == 0) {
                log.error("[Gateway] VNPT returned empty PDF body url={}", request.getPath());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            log.info("[Gateway] PDF downloaded size={}KB", response.getBody().length / 1024);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(response.getBody());

        } catch (RestClientResponseException ex) {
            log.error("[Gateway] forwardBinary failed status={} url={}", ex.getStatusCode(), request.getPath());
            return ResponseEntity.status(ex.getStatusCode()).build();
        } catch (Exception ex) {
            log.error("[Gateway] forwardBinary error url={}", request.getPath(), ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}