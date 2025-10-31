package com.gearup.configserver;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

@SpringBootTest(classes = ConfigServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigServerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void servesConfigFromRepo() {
        String url = "http://localhost:" + port + "/api-gateway/default";
    // config-server is secured with basic auth in application.yml (defaults used for tests)
    ResponseEntity<String> res = rest.withBasicAuth("configadmin", "change-me").getForEntity(url, String.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        // Response should include the application name and one of the configured properties
        assertThat(res.getBody()).contains("api-gateway");
        assertThat(res.getBody()).contains("redis");
    }
}
