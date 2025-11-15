package com.gearup.userauth.messaging;

import com.gearup.shared.event.ServiceTemplateCreatedEvent;
import com.gearup.shared.event.ServiceTemplateDeletedEvent;
import com.gearup.shared.event.ServiceTemplateUpdatedEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TemplateEventListener {

    private static final Logger log = LoggerFactory.getLogger(TemplateEventListener.class);

    @RabbitListener(
        queues = RabbitMQConstants.TEMPLATE_QUEUE,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onTemplateCreated(@Payload ServiceTemplateCreatedEvent event) {
        log.info("[TemplateEventListener] template.created received id={}, name={}", event.getTemplateId(), event.getName());
        // TODO: react as needed (e.g., permissions pre-warming, audit fan-out, cache)
    }

    @RabbitListener(
        queues = RabbitMQConstants.TEMPLATE_QUEUE,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onTemplateUpdated(@Payload ServiceTemplateUpdatedEvent event) {
        log.info("[TemplateEventListener] template.updated received id={}, name={}", event.getTemplateId(), event.getName());
    }

    @RabbitListener(
        queues = RabbitMQConstants.TEMPLATE_QUEUE,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onTemplateDeleted(@Payload ServiceTemplateDeletedEvent event) {
        log.info("[TemplateEventListener] template.deleted received id={}, name={}", event.getTemplateId(), event.getName());
    }
}
