package com.gearup.notificationservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitDiagnostics implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RabbitDiagnostics.class);

    private final AmqpAdmin amqpAdmin;
    private final ConnectionFactory connectionFactory;
    private final RabbitTemplate rabbitTemplate;

    public RabbitDiagnostics(AmqpAdmin amqpAdmin, ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate) {
        this.amqpAdmin = amqpAdmin;
        this.connectionFactory = connectionFactory;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("RabbitDiagnostics: AmqpAdmin present = {}", amqpAdmin != null);
        logger.info("RabbitDiagnostics: RabbitTemplate present = {}", rabbitTemplate != null);
        try {
            Connection conn = connectionFactory.createConnection();
            try {
                logger.info("RabbitDiagnostics: Successfully opened connection to RabbitMQ: {}", conn);
            } finally {
                conn.close();
            }
        } catch (Throwable t) {
            logger.error("RabbitDiagnostics: Failed to open connection to RabbitMQ", t);
        }
    }
}
