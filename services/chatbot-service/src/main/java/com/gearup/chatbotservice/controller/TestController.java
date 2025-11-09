package com.gearup.chatbotservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * Test Controller for public endpoints
 * Provides simple endpoints for testing and health checks
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class TestController {
    
    /**
     * Simple test endpoint
     * GET /api/v1/hello
     */
    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        log.info("Hello endpoint accessed");
        return ResponseEntity.ok("Hello There!");
    }
}
