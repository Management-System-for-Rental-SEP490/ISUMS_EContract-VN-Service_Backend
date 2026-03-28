package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import com.isums.econtractvnservice.exceptions.UnauthorizedException;
import com.isums.econtractvnservice.infrastructures.abstracts.GatewayAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class GatewayAuthServiceImpl implements GatewayAuthService {

    private final GatewayProperties properties;

    @Override
    public void validateInternalToken(String requestToken) {
        if (!StringUtils.hasText(requestToken) || !properties.getInternalToken().equals(requestToken)) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}