package com.isums.econtractvnservice.exceptions;

import com.isums.econtractvnservice.domains.dtos.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("UnauthorizedException -> 401")
    void unauthorized() {
        ResponseEntity<ErrorResponse> res = handler.handleUnauthorized(new UnauthorizedException("nope"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().getError()).isEqualTo("Unauthorized");
    }

    @Test
    @DisplayName("BadRequestException -> 400")
    void badRequest() {
        ResponseEntity<ErrorResponse> res = handler.handleBadRequest(new BadRequestException("x"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().getMessage()).isEqualTo("x");
    }

    @Test
    @DisplayName("HttpMessageNotReadableException -> 400 with helpful message")
    void unreadable() {
        HttpInputMessage msg = new HttpInputMessage() {
            @Override public InputStream getBody() { return new ByteArrayInputStream(new byte[0]); }
            @Override public HttpHeaders getHeaders() { return new HttpHeaders(); }
        };
        ResponseEntity<ErrorResponse> res = handler.handleUnreadable(
                new HttpMessageNotReadableException("bad", msg));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().getMessage()).contains("missing or invalid JSON");
    }

    @Test
    @DisplayName("generic Exception -> 500")
    void generic() {
        ResponseEntity<ErrorResponse> res = handler.handleException(new RuntimeException("boom"));
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
