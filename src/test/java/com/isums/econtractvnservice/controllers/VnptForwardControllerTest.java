package com.isums.econtractvnservice.controllers;

import com.isums.econtractvnservice.domains.dtos.MultipartForwardRequest;
import com.isums.econtractvnservice.exceptions.GlobalExceptionHandler;
import com.isums.econtractvnservice.exceptions.UnauthorizedException;
import com.isums.econtractvnservice.infrastructures.abstracts.GatewayAuthService;
import com.isums.econtractvnservice.infrastructures.abstracts.VnptForwardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("VnptForwardController")
class VnptForwardControllerTest {

    @Mock private GatewayAuthService gatewayAuthService;
    @Mock private VnptForwardService vnptForwardService;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks private VnptForwardController controller;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /forward validates token then delegates")
    void forward() throws Exception {
        org.mockito.Mockito.doReturn(ResponseEntity.ok("ok"))
                .when(vnptForwardService).forward(any());

        mvc.perform(post("/internal/vnpt/forward")
                        .header("X-Internal-Token", "t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"path\":\"/x\",\"method\":\"GET\"}"))
                .andExpect(status().isOk());

        verify(gatewayAuthService).validateInternalToken("t");
        verify(vnptForwardService).forward(any());
    }

    @Test
    @DisplayName("POST /forward returns 401 when token invalid (handler catches Unauthorized)")
    void forwardUnauthorized() throws Exception {
        doThrow(new UnauthorizedException("no"))
                .when(gatewayAuthService).validateInternalToken(anyString());

        mvc.perform(post("/internal/vnpt/forward")
                        .header("X-Internal-Token", "bad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"path\":\"/x\",\"method\":\"GET\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("POST /forward-multipart parses metadata JSON into MultipartForwardRequest")
    void forwardMultipart() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile("metadata",
                "", "text/plain", "{\"path\":\"/u\",\"method\":\"POST\"}".getBytes());
        MockMultipartFile file = new MockMultipartFile("file",
                "x.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[]{1, 2, 3});

        MultipartForwardRequest parsed = new MultipartForwardRequest();
        parsed.setPath("/u"); parsed.setMethod("POST");
        when(objectMapper.readValue(anyString(), eq(MultipartForwardRequest.class))).thenReturn(parsed);
        org.mockito.Mockito.doReturn(ResponseEntity.ok("ok"))
                .when(vnptForwardService).forwardMultipart(any(), any());

        mvc.perform(multipart("/internal/vnpt/forward-multipart")
                        .file(metadata).file(file)
                        .header("X-Internal-Token", "t"))
                .andExpect(status().isOk());

        verify(gatewayAuthService).validateInternalToken("t");
        verify(vnptForwardService).forwardMultipart(any(), any());
    }

    @Test
    @DisplayName("POST /test-post echoes body without requiring token")
    void testPost() throws Exception {
        mvc.perform(post("/internal/vnpt/test-post")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.body").value("hello"));
    }

    @Test
    @DisplayName("POST /forward-binary delegates to service")
    void forwardBinary() throws Exception {
        byte[] pdf = new byte[]{1, 2, 3};
        when(vnptForwardService.forwardBinary(any()))
                .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).body(pdf));

        mvc.perform(post("/internal/vnpt/forward-binary")
                        .header("X-Internal-Token", "t")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"path\":\"/d\"}"))
                .andExpect(status().isOk());

        verify(gatewayAuthService).validateInternalToken("t");
    }
}
