package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import com.isums.econtractvnservice.exceptions.UnauthorizedException;
import com.isums.econtractvnservice.infrastructures.abstracts.GatewayAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class GatewayAuthServiceImpl implements GatewayAuthService {

    private final GatewayProperties gatewayProperties;

    @Override
    public void validateInternalToken(String internalToken) {
        String expected = gatewayProperties.getInternalToken();
        if (!StringUtils.hasText(expected)) {
            return;
        }

        if (!StringUtils.hasText(internalToken)
                || !MessageDigest.isEqual(
                        internalToken.getBytes(StandardCharsets.UTF_8),
                        expected.getBytes(StandardCharsets.UTF_8))) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
