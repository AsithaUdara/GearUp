package com.gearup.apigateway.config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

@Configuration
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    @PostConstruct
    public void init() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            String envPath = System.getenv("FIREBASE_CONFIG_PATH");
            InputStream is = null;

            if (envPath != null && !envPath.trim().isEmpty()) {
                File f = new File(envPath);
                if (f.exists()) {
                    log.info("Using Firebase service account from environment path: {}", envPath);
                    is = new FileInputStream(f);
                } else {
                    log.warn("FIREBASE_CONFIG_PATH is set but file does not exist: {}", envPath);
                }
            }

            if (is == null && serviceAccountPath != null && !serviceAccountPath.isBlank()) {
                File f = new File(serviceAccountPath);
                if (f.exists()) {
                    log.info("Using Firebase service account from configured path: {}", serviceAccountPath);
                    is = new FileInputStream(f);
                } else {
                    log.warn("Configured firebase.service-account-path does not point to an existing file: {}", serviceAccountPath);
                }
            }

            if (is == null && serviceAccountJson != null && !serviceAccountJson.isBlank()) {
                log.info("Using Firebase service account from provided JSON property");
                is = new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8));
            }

            if (is == null) {
                InputStream cp = FirebaseConfig.class.getClassLoader().getResourceAsStream("firebase-service-account.json");
                if (cp != null) {
                    log.info("Using Firebase service account from classpath resource");
                    is = cp;
                }
            }

            if (is == null) {
                if ("prod".equals(System.getenv("SPRING_PROFILES_ACTIVE"))) {
                    throw new IllegalStateException("Firebase credentials required in production");
                }
                log.warn("No Firebase service account found; skipping initialization.");
                return;
            }

            try (InputStream fis = is) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(fis);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully.");
            }
        }
    }
}
