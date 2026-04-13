package com.isums.econtractvnservice.services;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import com.isums.econtractvnservice.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GatewayAuthServiceImpl")
class GatewayAuthServiceImplTest {

    private GatewayAuthServiceImpl service;
    private GatewayProperties properties;

    @BeforeEach
    void setUp() {
        properties = new GatewayProperties();
        properties.setInternalToken("secret-token-abc");
        service = new GatewayAuthServiceImpl(properties);
    }

    @Test
    @DisplayName("accepts the exact configured token")
    void validToken() {
        assertThatCode(() -> service.validateInternalToken("secret-token-abc"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("rejects null token")
    void nullToken() {
        assertThatThrownBy(() -> service.validateInternalToken(null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("rejects blank token")
    void blankToken() {
        assertThatThrownBy(() -> service.validateInternalToken("   "))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("rejects wrong token")
    void wrongToken() {
        assertThatThrownBy(() -> service.validateInternalToken("wrong"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("rejects partially correct token (length mismatch handled by MessageDigest.isEqual)")
    void partialMatch() {
        assertThatThrownBy(() -> service.validateInternalToken("secret"))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("rejects when server-side token not configured (defense-in-depth)")
    void serverTokenMissing() {
        properties.setInternalToken(null);
        assertThatThrownBy(() -> service.validateInternalToken("anything"))
                .isInstanceOf(UnauthorizedException.class);
    }
}
