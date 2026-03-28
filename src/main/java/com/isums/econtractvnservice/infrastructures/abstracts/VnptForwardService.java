package com.isums.econtractvnservice.infrastructures.abstracts;

import com.isums.econtractvnservice.domains.dtos.ForwardRequest;
import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface VnptForwardService {
    ResponseEntity<?> forward(ForwardRequest request);

    ResponseEntity<?> forwardMultipart(MultipartForwardRequest request, MultipartFile file);
}