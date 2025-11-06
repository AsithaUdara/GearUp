package com.gearup.service;

import com.gearup.events.PartsRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Configuration
@RequiredArgsConstructor
public class EventPublisher {
    private final Sinks.Many<Message<PartsRequestEvent>> sink = Sinks.many().multicast().onBackpressureBuffer();

    @Bean
    public Sinks.Many<Message<PartsRequestEvent>> partsRequestSink() {
        return sink;
    }

    @Bean
    public Flux<Message<PartsRequestEvent>> partsRequestPublisher() {
        return sink.asFlux();
    }

    public void publishPartsRequestEvent(PartsRequestEvent event) {
        Message<PartsRequestEvent> message = MessageBuilder.withPayload(event)
                .setHeader("eventType", event.getEventType())
                .build();
        sink.tryEmitNext(message);
    }
}