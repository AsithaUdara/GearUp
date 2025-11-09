package com.gearup.partsservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.gearup.partsservice.config.TestRabbitMQConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class, RedisAutoConfiguration.class})
class PartsServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test that the application context loads successfully
    }
}
