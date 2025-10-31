package com.gearup.apigateway.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream is = null;
            if (serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                is = new FileInputStream(serviceAccountPath);
            } else if (serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                is = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            }

            if (is != null) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(is);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }
    }
}
