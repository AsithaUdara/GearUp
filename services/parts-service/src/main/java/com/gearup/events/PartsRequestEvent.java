package com.gearup.events;

import com.gearup.domain.PartsRequestStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PartsRequestEvent {
    private UUID requestId;
    private String requestNumber;
    private String material;
    private Integer quantity;
    private PartsRequestStatus status;
    private UUID userId;
    private LocalDateTime timestamp;
    private String eventType;  // CREATED, STATUS_CHANGED

    public static PartsRequestEvent created(UUID requestId, String requestNumber, String material, 
                                          Integer quantity, UUID userId) {
        PartsRequestEvent event = new PartsRequestEvent();
        event.setRequestId(requestId);
        event.setRequestNumber(requestNumber);
        event.setMaterial(material);
        event.setQuantity(quantity);
        event.setStatus(PartsRequestStatus.PENDING);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("CREATED");
        return event;
    }

    public static PartsRequestEvent statusChanged(UUID requestId, String requestNumber, 
                                                PartsRequestStatus newStatus, UUID userId) {
        PartsRequestEvent event = new PartsRequestEvent();
        event.setRequestId(requestId);
        event.setRequestNumber(requestNumber);
        event.setStatus(newStatus);
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        event.setEventType("STATUS_CHANGED");
        return event;
    }
}