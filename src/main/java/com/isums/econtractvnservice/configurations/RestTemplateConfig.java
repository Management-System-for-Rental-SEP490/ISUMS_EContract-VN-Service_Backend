package com.isums.econtractvnservice.configurations;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final GatewayProperties gatewayProperties;

    @Bean
    public RestTemplate vnptRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(gatewayProperties.getConnectTimeoutMs());
        factory.setReadTimeout(gatewayProperties.getReadTimeoutMs());
        return new RestTemplate(factory);
    }
}