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

    private final GatewayProperties gatewayProperties;

    @Override
    public void validateInternalToken(String internalToken) {
        if (!StringUtils.hasText(internalToken)
                || !gatewayProperties.getInternalToken().equals(internalToken)) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}