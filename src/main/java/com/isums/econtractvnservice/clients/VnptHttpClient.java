package com.isums.econtractvnservice.clients;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

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

        System.out.println("=== VNPT HTTP CLIENT ===");
        System.out.println("URL: " + url);
        System.out.println("Method: " + method);
        System.out.println("Headers: " + headers);
        System.out.println("Body: " + body);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return vnptRestTemplate.exchange(url, method, entity, String.class);
    }
}