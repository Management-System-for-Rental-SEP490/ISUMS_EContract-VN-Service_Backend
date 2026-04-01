package com.isums.econtractvnservice.clients;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VnptHttpClient {

    private final RestTemplate vnptRestTemplate;
    private final GatewayProperties properties;

    public ResponseEntity<String> forward(
            String path,
            HttpMethod method,
            String body,
            Map<String, String> requestHeaders
    ) {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String url = properties.getVnptBaseUrl() + normalizedPath;

        HttpHeaders headers = new HttpHeaders();

        if (requestHeaders != null) {
            requestHeaders.forEach((k, v) -> {
                if (StringUtils.hasText(k) && StringUtils.hasText(v)) {
                    headers.add(k, v);
                }
            });
        }

        if (headers.getFirst(HttpHeaders.CONTENT_TYPE) == null && body != null && !body.isBlank()) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        log.info("Calling VNPT JSON. url={}, method={}, hasBody={}", url, method, body != null && !body.isBlank());

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return vnptRestTemplate.exchange(url, method, entity, String.class);
    }

    public ResponseEntity<String> forwardMultipart(
            String path,
            HttpMethod method,
            Map<String, String> requestHeaders,
            Map<String, String> formFields,
            MultipartFile file
    ) throws IOException {
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        String url = properties.getVnptBaseUrl() + normalizedPath;

        HttpHeaders headers = new HttpHeaders();

        if (requestHeaders != null) {
            requestHeaders.forEach((k, v) -> {
                if (StringUtils.hasText(k)
                        && StringUtils.hasText(v)
                        && !HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(k)
                        && !HttpHeaders.CONTENT_LENGTH.equalsIgnoreCase(k)) {
                    headers.add(k, v);
                }
            });
        }

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        if (formFields != null) {
            formFields.forEach(parts::add);
        }

        if (file != null && !file.isEmpty()) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_PDF);

            HttpEntity<ByteArrayResource> fileEntity = new HttpEntity<>(resource, fileHeaders);
            parts.add("File", fileEntity);
        }

        log.info("Calling VNPT MULTIPART. url={}, method={}, hasFile={}", url, method, file != null && !file.isEmpty());

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(parts, headers);
        return vnptRestTemplate.exchange(url, method, entity, String.class);
    }

    public ResponseEntity<byte[]> forwardBinary(String downloadUrl, Map<String, String> requestHeaders
    ) {
        HttpHeaders headers = new HttpHeaders();
        if (requestHeaders != null) {
            requestHeaders.forEach((k, v) -> {
                if (StringUtils.hasText(k) && StringUtils.hasText(v)) {
                    headers.add(k, v);
                }
            });
        }
        headers.setAccept(List.of(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM));

        log.info("[Gateway] forwardBinary downloading from={}", downloadUrl);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            return vnptRestTemplate.exchange(downloadUrl, HttpMethod.GET, entity, byte[].class);
        } catch (RestClientResponseException ex) {
            log.error("[Gateway] VNPT download failed status={} url={}", ex.getStatusCode(), downloadUrl);
            log.error("[Gateway] VNPT response body={}", ex.getResponseBodyAsString());
            throw ex;
        }
    }
}