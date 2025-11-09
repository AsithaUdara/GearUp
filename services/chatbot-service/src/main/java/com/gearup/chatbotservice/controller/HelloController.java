package com.gearup.chatbotservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        log.debug("GET /api/v1/hello -> returning world");
        return ResponseEntity.ok("world");
    }
}
