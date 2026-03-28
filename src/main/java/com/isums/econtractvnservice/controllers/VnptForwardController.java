package com.isums.econtractvnservice.controllers;

import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.infrastructures.abstracts.GatewayAuthService;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/vnpt")
@RequiredArgsConstructor
public class VnptForwardController {

    private final GatewayAuthService gatewayAuthService;
    private final VnptForwardService vnptForwardService;

    @PostMapping("/forward")
    public ResponseEntity<?> forward(@RequestHeader("X-Internal-Token") String internalToken, @Valid @RequestBody ForwardRequest request
    ) {
        gatewayAuthService.validateInternalToken(internalToken);
        return vnptForwardService.forward(request);
    }
}