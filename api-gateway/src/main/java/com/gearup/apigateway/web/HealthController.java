package com.gearup.apigateway.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping(value = "/healthz", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> healthz() {
        return Map.of("status", "UP");
    }
}
