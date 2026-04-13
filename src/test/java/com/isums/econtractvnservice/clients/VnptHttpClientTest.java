package com.isums.econtractvnservice.clients;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@DisplayName("VnptHttpClient")
class VnptHttpClientTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private VnptHttpClient client;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        GatewayProperties props = new GatewayProperties();
        props.setVnptBaseUrl("https://vnpt.example");
        client = new VnptHttpClient(restTemplate, props);
    }

    @Test
    @DisplayName("forward appends base URL, adds leading slash, sets JSON Content-Type when body present")
    void forwardAddsLeadingSlashAndJson() {
        server.expect(once(), requestTo("https://vnpt.example/api/x"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{\"a\":1}"))
                .andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

        ResponseEntity<String> res = client.forward("api/x", HttpMethod.POST, "{\"a\":1}", null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("ok");
        server.verify();
    }

    @Test
    @DisplayName("forward respects caller's Content-Type")
    void respectsCallerContentType() {
        server.expect(requestTo("https://vnpt.example/api/y"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, "application/xml"))
                .andRespond(withSuccess("ok", MediaType.TEXT_PLAIN));

        client.forward("/api/y", HttpMethod.POST, "<x/>",
                Map.of("Content-Type", "application/xml"));

        server.verify();
    }

    @Test
    @DisplayName("forward ignores null/blank header values")
    void ignoresBlankHeaders() {
        server.expect(requestTo("https://vnpt.example/api/z"))
                .andExpect(header("X-Kept", "v"))
                .andRespond(withSuccess());

        Map<String, String> headers = new java.util.HashMap<>();
        headers.put("X-Kept", "v");
        headers.put("", "empty-key-skipped");
        headers.put("X-Blank", "");

        client.forward("/api/z", HttpMethod.GET, null, headers);
        server.verify();
    }

    @Test
    @DisplayName("forwardMultipart drops Content-Type + Content-Length from request headers and sends multipart")
    void multipartStripsCtypeAndLength() throws Exception {
        server.expect(once(), requestTo("https://vnpt.example/upload"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Auth", "tok"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE,
                        org.hamcrest.Matchers.containsString(MediaType.MULTIPART_FORM_DATA_VALUE)))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        MockMultipartFile file = new MockMultipartFile("file", "x.pdf",
                MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});

        Map<String, String> headers = Map.of(
                "X-Auth", "tok",
                "Content-Type", "application/json",   // must be stripped
                "Content-Length", "999"               // must be stripped
        );

        client.forwardMultipart("/upload", HttpMethod.POST, headers,
                Map.of("docId", "D1"), file);

        server.verify();
    }

    @Test
    @DisplayName("forwardBinary requests PDF/OCTET_STREAM Accept and returns bytes")
    void binary() {
        byte[] pdf = new byte[]{(byte) 0x25, (byte) 0x50, (byte) 0x44, (byte) 0x46}; // %PDF
        server.expect(requestTo("https://vnpt.example/d.pdf"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.ACCEPT,
                        org.hamcrest.Matchers.containsString(MediaType.APPLICATION_PDF_VALUE)))
                .andRespond(withSuccess(pdf, MediaType.APPLICATION_PDF));

        ResponseEntity<byte[]> res = client.forwardBinary("https://vnpt.example/d.pdf", null);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isEqualTo(pdf);
    }

    @Test
    @DisplayName("forwardBinary rethrows RestClientResponseException on 4xx so service sees the status code")
    void binary4xx() {
        server.expect(requestTo("https://vnpt.example/missing"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.RestClientResponseException.class,
                () -> client.forwardBinary("https://vnpt.example/missing", null));
    }
}
