package com.isums.econtractvnservice;

import com.isums.econtractvnservice.configurations.GatewayProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(GatewayProperties.class)
public class EcontractVnServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcontractVnServiceApplication.class, args);
    }

}
