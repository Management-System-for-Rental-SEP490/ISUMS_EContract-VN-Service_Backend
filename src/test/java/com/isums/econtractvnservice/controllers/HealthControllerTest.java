package com.isums.econtractvnservice.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("HealthController")
class HealthControllerTest {

    @Test
    @DisplayName("GET / returns service/status/time payload")
    void root() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new HealthController()).build();

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("vnpt-gateway"))
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.time").isNotEmpty());
    }
}
