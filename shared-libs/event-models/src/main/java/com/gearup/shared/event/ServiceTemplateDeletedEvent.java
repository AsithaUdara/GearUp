package com.gearup.shared.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Event fired when a service template is deleted.
 */
public class ServiceTemplateDeletedEvent implements Serializable {
    private Long templateId;
    private String name;
    private String performedBy; // firebase uid
    private LocalDateTime occurredAt;

    public ServiceTemplateDeletedEvent() {}

    public ServiceTemplateDeletedEvent(Long templateId, String name, String performedBy, LocalDateTime occurredAt) {
        this.templateId = templateId;
        this.name = name;
        this.performedBy = performedBy;
        this.occurredAt = occurredAt;
    }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }
}
