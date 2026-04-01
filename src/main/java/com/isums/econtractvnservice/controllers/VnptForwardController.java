package com.isums.econtractvnservice.controllers;

import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import com.isums.econtractvnservice.infrastructures.abstracts.GatewayAuthService;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/internal/vnpt")
@RequiredArgsConstructor
public class VnptForwardController {

    private final GatewayAuthService gatewayAuthService;
    private final VnptForwardService vnptForwardService;
    private final ObjectMapper objectMapper;

    @PostMapping("/forward")
    public ResponseEntity<?> forward(@RequestHeader("X-Internal-Token") String internalToken, @Valid @RequestBody ForwardRequest request
    ) {
        gatewayAuthService.validateInternalToken(internalToken);
        return vnptForwardService.forward(request);
    }

    @PostMapping("/forward-multipart")
    public ResponseEntity<?> forwardMultipart(@RequestHeader("X-Internal-Token") String internalToken, @RequestPart("metadata") String metadata,
                                              @RequestPart(value = "file", required = false) MultipartFile file
    ) throws Exception {
        gatewayAuthService.validateInternalToken(internalToken);

        MultipartForwardRequest request = objectMapper.readValue(metadata, MultipartForwardRequest.class);
        return vnptForwardService.forwardMultipart(request, file);
    }

    @PostMapping("/forward-binary")
    public ResponseEntity<byte[]> forwardBinary(@RequestHeader("X-Internal-Token") String token, @RequestBody ForwardRequest request) {
        gatewayAuthService.validateInternalToken(token);
        return vnptForwardService.forwardBinary(request);
    }

    @PostMapping("/test-post")
    public ResponseEntity<?> testPost(@RequestBody(required = false) String body) {
        return ResponseEntity.ok(Map.of("ok", true, "body", body));
    }
}