package com.gearup.userauth.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forces early declaration of exchanges, queues and bindings via RabbitAdmin to
 * avoid passive declaration failures in listeners on first startup.
 */
@Configuration
public class MessagingBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MessagingBootstrap.class);

    private final RabbitAdmin rabbitAdmin;
    private final Queue templateQueue;
    private final TopicExchange templateExchange;
    private final Binding templateCreatedBinding;
    private final Binding templateUpdatedBinding;
    private final Binding templateDeletedBinding;

    public MessagingBootstrap(RabbitAdmin rabbitAdmin,
                              Queue templateQueue,
                              TopicExchange templateExchange,
                              Binding templateCreatedBinding,
                              Binding templateUpdatedBinding,
                              Binding templateDeletedBinding) {
        this.rabbitAdmin = rabbitAdmin;
        this.templateQueue = templateQueue;
        this.templateExchange = templateExchange;
        this.templateCreatedBinding = templateCreatedBinding;
        this.templateUpdatedBinding = templateUpdatedBinding;
        this.templateDeletedBinding = templateDeletedBinding;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Explicit declarations (idempotent) to guarantee presence before listeners start
        log.info("[MessagingBootstrap] Declaring template exchange {}, queue {} and bindings", templateExchange.getName(), templateQueue.getName());
        rabbitAdmin.declareExchange(templateExchange);
        rabbitAdmin.declareQueue(templateQueue);
        rabbitAdmin.declareBinding(templateCreatedBinding);
        rabbitAdmin.declareBinding(templateUpdatedBinding);
        rabbitAdmin.declareBinding(templateDeletedBinding);
        log.info("[MessagingBootstrap] RabbitMQ declarations completed.");
    }
}
