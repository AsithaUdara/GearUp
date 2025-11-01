package com.gearup.apigateway.service;

import org.springframework.stereotype.Component;

import com.google.firebase.auth.FirebaseAuth;

@Component
public class FirebaseAuthProvider {
    public FirebaseAuth get() {
        return FirebaseAuth.getInstance();
    }
}
