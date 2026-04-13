package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.clients.VnptHttpClient;
import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import com.isums.econtractvnservice.exceptions.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("VnptForwardServiceImpl")
class VnptForwardServiceImplTest {

    @Mock private VnptHttpClient vnptHttpClient;

    @InjectMocks private VnptForwardServiceImpl service;

    private ForwardRequest req;

    @BeforeEach
    void setUp() {
        req = new ForwardRequest();
        req.setPath("/api/x");
        req.setMethod("POST");
        req.setBody("{}");
    }

    @Nested
    @DisplayName("forward (JSON)")
    class Forward {

        @Test
        @DisplayName("returns the body+status from VNPT response with its Content-Type")
        void happy() {
            HttpHeaders vnptHeaders = new HttpHeaders();
            vnptHeaders.setContentType(MediaType.APPLICATION_JSON);
            when(vnptHttpClient.forward(eq("/api/x"), eq(HttpMethod.POST), eq("{}"), any()))
                    .thenReturn(new ResponseEntity<>("{\"ok\":true}", vnptHeaders, HttpStatus.OK));

            ResponseEntity<?> res = service.forward(req);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isEqualTo("{\"ok\":true}");
            assertThat(res.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }

        @Test
        @DisplayName("passes through VNPT 4xx response body+status (does not bubble as 502)")
        void vnpt4xxPassthrough() {
            HttpHeaders vnptHeaders = new HttpHeaders();
            vnptHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpClientErrorException ex = HttpClientErrorException.create(
                    HttpStatus.BAD_REQUEST, "bad", vnptHeaders,
                    "{\"err\":\"x\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            when(vnptHttpClient.forward(any(), any(), any(), any())).thenThrow(ex);

            ResponseEntity<?> res = service.forward(req);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(res.getBody()).isEqualTo("{\"err\":\"x\"}");
            assertThat(res.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        }

        @Test
        @DisplayName("returns 502 BAD_GATEWAY on unexpected client error")
        void unexpected() {
            when(vnptHttpClient.forward(any(), any(), any(), any()))
                    .thenThrow(new RuntimeException("connection refused"));

            ResponseEntity<?> res = service.forward(req);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }

        @Test
        @DisplayName("throws BadRequest when path missing")
        void missingPath() {
            req.setPath(null);
            assertThatThrownBy(() -> service.forward(req))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("throws BadRequest when method missing")
        void missingMethod() {
            req.setMethod("");
            assertThatThrownBy(() -> service.forward(req))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("throws BadRequest on invalid HTTP method")
        void badMethod() {
            req.setMethod("WHOOPS");
            assertThatThrownBy(() -> service.forward(req))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("uppercases method before resolving (lowercase 'get' still accepted)")
        void lowercaseMethod() {
            req.setMethod("get");
            req.setBody(null);
            when(vnptHttpClient.forward(any(), eq(HttpMethod.GET), any(), any()))
                    .thenReturn(ResponseEntity.ok("x"));

            assertThat(service.forward(req).getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("forwardMultipart")
    class Multipart {

        @Test
        @DisplayName("passes metadata + file through to client and returns response body")
        void happy() throws Exception {
            MultipartForwardRequest mpReq = new MultipartForwardRequest();
            mpReq.setPath("/upload"); mpReq.setMethod("POST");
            mpReq.setHeaders(Map.of("X-Auth", "v"));
            mpReq.setFormFields(Map.of("docId", "D1"));

            MockMultipartFile file = new MockMultipartFile("file", "x.pdf",
                    MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});

            when(vnptHttpClient.forwardMultipart(eq("/upload"), eq(HttpMethod.POST),
                    anyMap(), anyMap(), eq(file)))
                    .thenReturn(ResponseEntity.ok("ok"));

            ResponseEntity<?> res = service.forwardMultipart(mpReq, file);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isEqualTo("ok");
        }

        @Test
        @DisplayName("throws BadRequest when path/method missing")
        void validation() {
            MultipartForwardRequest mpReq = new MultipartForwardRequest();
            mpReq.setPath(""); mpReq.setMethod("POST");

            assertThatThrownBy(() -> service.forwardMultipart(mpReq, null))
                    .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("passes through VNPT 4xx errors")
        void vnpt4xx() throws Exception {
            MultipartForwardRequest mpReq = new MultipartForwardRequest();
            mpReq.setPath("/upload"); mpReq.setMethod("POST");

            HttpClientErrorException ex = HttpClientErrorException.create(
                    HttpStatus.UNPROCESSABLE_ENTITY, "x", new HttpHeaders(),
                    "bad".getBytes(), StandardCharsets.UTF_8);
            when(vnptHttpClient.forwardMultipart(any(), any(), any(), any(), any())).thenThrow(ex);

            ResponseEntity<?> res = service.forwardMultipart(mpReq, null);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
            assertThat(res.getBody()).isEqualTo("bad");
        }
    }

    @Nested
    @DisplayName("forwardBinary")
    class Binary {

        @Test
        @DisplayName("returns PDF body with Content-Type application/pdf")
        void happy() {
            byte[] pdf = new byte[]{1, 2, 3, 4};
            when(vnptHttpClient.forwardBinary(eq("/download"), any()))
                    .thenReturn(ResponseEntity.ok(pdf));

            ForwardRequest r = new ForwardRequest();
            r.setPath("/download");

            ResponseEntity<byte[]> res = service.forwardBinary(r);

            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(res.getBody()).isEqualTo(pdf);
            assertThat(res.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        }

        @Test
        @DisplayName("returns 502 when VNPT responds with empty body")
        void emptyBody() {
            when(vnptHttpClient.forwardBinary(any(), any()))
                    .thenReturn(ResponseEntity.ok(new byte[0]));

            ForwardRequest r = new ForwardRequest();
            r.setPath("/d");

            ResponseEntity<byte[]> res = service.forwardBinary(r);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }

        @Test
        @DisplayName("passes VNPT 4xx status through (empty body)")
        void vnpt4xx() {
            HttpClientErrorException ex = HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND, "x", new HttpHeaders(), new byte[0], StandardCharsets.UTF_8);
            when(vnptHttpClient.forwardBinary(any(), any())).thenThrow(ex);

            ForwardRequest r = new ForwardRequest();
            r.setPath("/d");

            ResponseEntity<byte[]> res = service.forwardBinary(r);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("returns 502 on unexpected error")
        void unexpected() {
            when(vnptHttpClient.forwardBinary(any(), any()))
                    .thenThrow(new RuntimeException("io"));

            ForwardRequest r = new ForwardRequest();
            r.setPath("/d");

            ResponseEntity<byte[]> res = service.forwardBinary(r);
            assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }

        @Test
        @DisplayName("throws BadRequest when path missing")
        void missingPath() {
            ForwardRequest r = new ForwardRequest();
            assertThatThrownBy(() -> service.forwardBinary(r))
                    .isInstanceOf(BadRequestException.class);
        }
    }
}
