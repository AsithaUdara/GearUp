package com.gearup.shared.event.tracking;

import com.gearup.shared.event.base.BaseTrackingEvent;

import java.time.LocalDateTime;

/**
 * Event published when a task is started
 */
public class TaskStartedEvent extends BaseTrackingEvent {
    
    private static final long serialVersionUID = 1L;
    
    private LocalDateTime startedAt;
    private String serviceType;
    private String customer;
    private Integer progressStep;
    
    public TaskStartedEvent() {
        super();
    }
    
    public TaskStartedEvent(String eventId, String taskId, String assigneeId,
                           LocalDateTime startedAt, String serviceType, String customer, Integer progressStep) {
        super(eventId, taskId, assigneeId);
        this.startedAt = startedAt;
        this.serviceType = serviceType;
        this.customer = customer;
        this.progressStep = progressStep;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public Integer getProgressStep() {
        return progressStep;
    }

    public void setProgressStep(Integer progressStep) {
        this.progressStep = progressStep;
    }
}
