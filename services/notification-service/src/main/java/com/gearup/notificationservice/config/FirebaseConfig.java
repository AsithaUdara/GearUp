package com.gearup.notificationservice.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${app.firebase-configuration-file:}")
    private Resource serviceAccount;

    @PostConstruct
    public void initialize() {
        String activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        boolean isDev = "dev".equals(activeProfile) || activeProfile == null;
        
        try {
            InputStream in = null;
            
            // Option 1: Try to read from FIREBASE_SERVICE_ACCOUNT_JSON env variable (for dev/CI)
            String jsonCredentials = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");
            if (jsonCredentials != null && !jsonCredentials.trim().isEmpty()) {
                log.info("Using Firebase credentials from FIREBASE_SERVICE_ACCOUNT_JSON environment variable");
                // Replace escaped newlines with actual newlines for private key
                String processedJson = jsonCredentials.replace("\\n", "\n");
                in = new java.io.ByteArrayInputStream(processedJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            // Option 2: Try environment path
            if (in == null) {
                String envPath = System.getenv("FIREBASE_CONFIG_PATH");
                if (envPath != null && !envPath.trim().isEmpty()) {
                    File f = new File(envPath);
                    if (f.exists()) {
                        log.info("Using Firebase service account from environment path: {}", envPath);
                        in = new FileInputStream(f);
                    } else {
                        log.warn("FIREBASE_CONFIG_PATH is set but file does not exist: {}", envPath);
                    }
                }
            }

            // Option 3: Try property file
            if (in == null && serviceAccount != null) {
                try {
                    if (serviceAccount.exists()) {
                        log.info("Using Firebase service account from property: {}", serviceAccount.getURI());
                        in = serviceAccount.getInputStream();
                    }
                } catch (IOException e) {
                    log.warn("Configured serviceAccount resource cannot be read", e);
                }
            }

            // Option 4: Try classpath
            if (in == null) {
                InputStream cp = FirebaseConfig.class.getClassLoader().getResourceAsStream("firebase-service-account.json");
                if (cp != null) {
                    log.info("Using Firebase service account from classpath resource");
                    in = cp;
                }
            }

            if (in == null) {
                if ("prod".equals(activeProfile)) {
                    throw new IllegalStateException("Firebase credentials required in production");
                }
                log.warn("No Firebase service account found; skipping initialization. Running in {} mode.", isDev ? "dev" : "test");
                return;
            }

            try (InputStream serviceAccountStream = in) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                    log.info("Firebase initialized successfully.");
                }
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            // In dev mode, log warning but don't fail startup
            if (isDev) {
                log.warn("Continuing without Firebase in dev mode");
            } else {
                throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
            }
        }
    }
}
