package com.gearup.analyticalservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventListener {

    @RabbitListener(queues = "#{analyticsQueue.name}")
    public void handleAllEvents(Object event) {
        try {
            String eventType = event.getClass().getSimpleName();
            log.info("[ANALYTICS] Received Event: {} - {}", eventType, event);
            
        } catch (Exception e) {
            log.error("[ANALYTICS] Failed to process event", e);
        }
    }
}
