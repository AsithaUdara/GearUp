package com.gearup.automobileservice.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Service
public class FirebaseConfig {


    @Value("${app.firebase-configuration-file}")
    private Resource serviceAccount;

    @PostConstruct
    public void initialize() {
        try {
            if (!serviceAccount.exists()) {
                throw new IOException("Firebase service account file not found at path: " + serviceAccount.getURI());
            }
            InputStream serviceAccountStream = serviceAccount.getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
