package com.gearup.apigateway;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public class RateLimiterE2ETest {

    @Container
    @SuppressWarnings("resource")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2.2").withExposedPorts(6379);

    static MockWebServer mockWebServer;

    @LocalServerPort
    int port;

    @BeforeAll
    static void startMock() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void stopMock() throws Exception {
        if (mockWebServer != null) mockWebServer.shutdown();
        if (redis != null) redis.stop();
    }

    @DynamicPropertySource
    static void setProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

        // Configure a test route that forwards /e2e/** to the MockWebServer
        registry.add("spring.cloud.gateway.server.webflux.routes[0].id", () -> "e2e-route");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].uri", () -> mockWebServer.url("/").toString());
        registry.add("spring.cloud.gateway.server.webflux.routes[0].predicates[0]", () -> "Path=/e2e/**");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[0].name", () -> "RequestRateLimiter");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[0].args.redis-rate-limiter.replenishRate", () -> "5");
        registry.add("spring.cloud.gateway.server.webflux.routes[0].filters[0].args.redis-rate-limiter.burstCapacity", () -> "10");
    }

    @Test
    void gatewayShouldThrottleAfterBurst() {
        // Enqueue many OK responses from downstream
        for (int i = 0; i < 50; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("ok"));
        }

        WebClient client = WebClient.create("http://localhost:" + port);

        int ok = 0;
        int throttled = 0;
        for (int i = 0; i < 50; i++) {
            Integer status = client.get()
                    .uri("/e2e/ping")
                    .exchangeToMono(response -> Mono.just(response.statusCode().value()))
                    .block();
            if (status == 200) ok++; else if (status == 429) throttled++;
        }

        // Expect at least some requests to be throttled given replenishRate=5 and burstCapacity=10
        assertThat(throttled).isGreaterThan(0);
        assertThat(ok + throttled).isEqualTo(50);
    }
}
