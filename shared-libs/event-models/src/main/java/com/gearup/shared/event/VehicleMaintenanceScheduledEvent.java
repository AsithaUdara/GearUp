package com.gearup.shared.event;

import java.time.LocalDateTime;

/**
 * Event published when vehicle maintenance is scheduled.
 */
public class VehicleMaintenanceScheduledEvent extends BaseNotificationEvent {
    
    private String maintenanceId;
    private String vehicleId;
    private String vehicleName;
    private String maintenanceType;
    private LocalDateTime scheduledDate;
    private String serviceCenter;
    private Double estimatedCost;

    public VehicleMaintenanceScheduledEvent() {
        super();
    }

    public VehicleMaintenanceScheduledEvent(String eventId, String userId, LocalDateTime timestamp,
                                           String maintenanceId, String vehicleId, String vehicleName,
                                           String maintenanceType, LocalDateTime scheduledDate,
                                           String serviceCenter, Double estimatedCost) {
        super(eventId, userId, timestamp);
        this.maintenanceId = maintenanceId;
        this.vehicleId = vehicleId;
        this.vehicleName = vehicleName;
        this.maintenanceType = maintenanceType;
        this.scheduledDate = scheduledDate;
        this.serviceCenter = serviceCenter;
        this.estimatedCost = estimatedCost;
    }

    public String getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(String maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getMaintenanceType() {
        return maintenanceType;
    }

    public void setMaintenanceType(String maintenanceType) {
        this.maintenanceType = maintenanceType;
    }

    public LocalDateTime getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDateTime scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getServiceCenter() {
        return serviceCenter;
    }

    public void setServiceCenter(String serviceCenter) {
        this.serviceCenter = serviceCenter;
    }

    public Double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(Double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    @Override
    public String toString() {
        return "VehicleMaintenanceScheduledEvent{" +
                "eventId='" + getEventId() + '\'' +
                ", userId='" + getUserId() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", maintenanceId='" + maintenanceId + '\'' +
                ", vehicleId='" + vehicleId + '\'' +
                ", vehicleName='" + vehicleName + '\'' +
                ", maintenanceType='" + maintenanceType + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", serviceCenter='" + serviceCenter + '\'' +
                ", estimatedCost=" + estimatedCost +
                '}';
    }
}
