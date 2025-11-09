package com.gearup.templateservice.messaging;

import com.gearup.shared.event.ServiceTemplateCreatedEvent;
import com.gearup.shared.event.ServiceTemplateDeletedEvent;
import com.gearup.shared.event.ServiceTemplateUpdatedEvent;
import com.gearup.shared.messaging.RabbitMQConfig;
import com.gearup.templateservice.dto.ServiceTemplateDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TemplateEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TemplateEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public TemplateEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(ServiceTemplateDto template, String performedBy) {
        ServiceTemplateCreatedEvent event = new ServiceTemplateCreatedEvent(
                template.getId(), template.getName(), template.getDescription(),
                template.getPrice(),
                template.getDurationMinutes(), template.getActive(), performedBy, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.TEMPLATE_EXCHANGE, RabbitMQConfig.TEMPLATE_CREATED_KEY, event);
        log.info("Published template.created event for id={}, name={}", template.getId(), template.getName());
    }

    public void publishUpdated(ServiceTemplateDto template, String performedBy) {
        ServiceTemplateUpdatedEvent event = new ServiceTemplateUpdatedEvent(
                template.getId(), template.getName(), template.getDescription(),
                template.getPrice(),
                template.getDurationMinutes(), template.getActive(), performedBy, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.TEMPLATE_EXCHANGE, RabbitMQConfig.TEMPLATE_UPDATED_KEY, event);
        log.info("Published template.updated event for id={}, name={}", template.getId(), template.getName());
    }

    public void publishDeleted(Long templateId, String name, String performedBy) {
        ServiceTemplateDeletedEvent event = new ServiceTemplateDeletedEvent(
                templateId, name, performedBy, LocalDateTime.now()
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.TEMPLATE_EXCHANGE, RabbitMQConfig.TEMPLATE_DELETED_KEY, event);
        log.info("Published template.deleted event for id={}, name={}", templateId, name);
    }
}
