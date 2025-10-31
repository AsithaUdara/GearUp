package com.gearup.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class FirebaseAuthenticationFilterTest {

    @Test
    void canInstantiateFilter() {
        FirebaseAuthenticationFilter f = new FirebaseAuthenticationFilter();
        assertNotNull(f);
    }
}
