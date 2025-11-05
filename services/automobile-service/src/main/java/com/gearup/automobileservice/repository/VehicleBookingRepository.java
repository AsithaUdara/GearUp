package com.gearup.automobileservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gearup.automobileservice.entity.VehicleBooking;

@Repository
public interface VehicleBookingRepository extends JpaRepository<VehicleBooking, String> {
    
    List<VehicleBooking> findByUserId(String userId);
    
    List<VehicleBooking> findByVehicleId(String vehicleId);
    
    List<VehicleBooking> findByStatus(VehicleBooking.BookingStatus status);
}
