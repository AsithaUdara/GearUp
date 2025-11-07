package com.gearup.chatbotservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;

/**
 * Test configuration to provide mock beans for external dependencies
 * This prevents test failures when external services (RabbitMQ, Redis) are not available
 * Only active for @DataJpaTest with "test" profile
 */
@TestConfiguration
@Profile("test")
public class TestRabbitMQConfig {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return mock(ConnectionFactory.class);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate() {
        return mock(RabbitTemplate.class);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }
}
