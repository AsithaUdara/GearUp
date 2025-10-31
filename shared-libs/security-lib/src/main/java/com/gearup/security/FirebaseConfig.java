package com.gearup.security;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * Shared Firebase configuration initializer. Services should include the
 * property `app.firebase-configuration-file` pointing to the service account
 * JSON file (classpath: or filesystem) so this initializer can load it.
 */
@Component
public class FirebaseConfig {

    @Value("${app.firebase-configuration-file}")
    private Resource serviceAccount;

    @PostConstruct
    public void initialize() {
        try {
            if (serviceAccount == null || !serviceAccount.exists()) {
                throw new IOException("Firebase service account file not found at: " + (serviceAccount == null ? "<null>" : serviceAccount.getURI()));
            }

            try (InputStream serviceAccountStream = serviceAccount.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firebase: " + e.getMessage(), e);
        }
    }
}
