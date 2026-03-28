package com.isums.econtractvnservice.infrastructures.abstracts;

import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import org.springframework.http.ResponseEntity;

public interface VnptForwardService {
    ResponseEntity<?> forward(ForwardRequest request);
}
