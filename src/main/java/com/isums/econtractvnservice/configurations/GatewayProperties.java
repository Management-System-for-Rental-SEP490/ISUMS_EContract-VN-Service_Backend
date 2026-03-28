package com.isums.econtractvnservice.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties {
    private String internalToken;
    private String vnptBaseUrl;
    private int connectTimeoutMs;
    private int readTimeoutMs;
}