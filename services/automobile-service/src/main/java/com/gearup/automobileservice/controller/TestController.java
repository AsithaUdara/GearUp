package com.gearup.automobileservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/public/hello")
    public String publicEndpoint() {
        return "Hello from a PUBLIC endpoint! Anyone can see this.";
    }

    @GetMapping("/secure/hello")
    public String secureEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return "Hello from a SECURE endpoint! Your Firebase User ID is: " + userId;
    }
}
