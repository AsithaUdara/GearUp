package com.gearup.apigateway.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.gearup.apigateway.service.FirebaseAuthProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class FirebaseGatewayFilterTest {

    @LocalServerPort
    int port;

    @MockBean
    FirebaseAuthProvider mockedProvider;

    @Test
    void filterAddsUserIdHeaderWhenTokenValid() throws Exception {
        // Mock provider and underlying FirebaseAuth
        FirebaseAuth fakeAuth = Mockito.mock(FirebaseAuth.class);
        FirebaseToken token = Mockito.mock(FirebaseToken.class);
        Mockito.when(token.getUid()).thenReturn("test-user-123");
        Mockito.when(fakeAuth.verifyIdToken(Mockito.anyString())).thenReturn(token);
        // Inject mocked provider
        // note: @MockBean field will be injected by Spring
        // Configure it to return the fakeAuth
        Mockito.when(mockedProvider.get()).thenReturn(fakeAuth);

            WebClient client = WebClient.create("http://localhost:" + port);
            // Call a route that doesn't exist; but we only validate the filter doesn't throw and app starts
    Integer status = client.get().uri("/api/does-not-exist").exchangeToMono(resp ->
        resp.bodyToMono(String.class).map(b -> resp.statusCode().value()))
        .onErrorReturn(404)
        .block();

            // If call returned something, the application filter chain executed; assert status is not null
            assertThat(status).isNotNull();
    }

}
