package com.gearup.shared.event.vehicle;

import com.gearup.shared.event.base.BaseVehicleEvent;

import java.time.LocalDateTime;

/**
 * Event published when a vehicle is registered
 */
public class VehicleRegisteredEvent extends BaseVehicleEvent {
    
    private static final long serialVersionUID = 1L;
    
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String vin;
    private LocalDateTime registeredAt;
    
    public VehicleRegisteredEvent() {
        super();
    }
    
    public VehicleRegisteredEvent(String eventId, Long vehicleId, String customerId,
                                 String make, String model, Integer year,
                                 String licensePlate, String vin, LocalDateTime registeredAt) {
        super(eventId, vehicleId, customerId);
        this.make = make;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.vin = vin;
        this.registeredAt = registeredAt;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
