package com.isums.econtractvnservice.infrastructures.abstracts;

public interface GatewayAuthService {
    void validateInternalToken(String requestToken);
}
