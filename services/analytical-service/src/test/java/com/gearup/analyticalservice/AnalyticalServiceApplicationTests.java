package com.gearup.analyticalservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.gearup.analyticalservice.config.TestRabbitMQConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestRabbitMQConfig.class)
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class, RedisAutoConfiguration.class})
class AnalyticalServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
