package com.gearup.appointmentservice.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gearup.appointmentservice.entity.TimeSlot;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByServiceIdAndSlotDateAndIsAvailableTrue(Long serviceId, LocalDate slotDate);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.service.id = :serviceId AND ts.slotDate = :date AND ts.isAvailable = true ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsByServiceAndDate(@Param("serviceId") Long serviceId, @Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.slotDate = :date AND ts.isAvailable = true ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsByDate(@Param("date") LocalDate date);
    
    // Query for customer-facing endpoints: excludes slots with active bookings
    @Query("SELECT ts FROM TimeSlot ts " +
           "WHERE ts.service.id = :serviceId " +
           "AND ts.slotDate = :date " +
           "AND ts.isAvailable = true " +
           "AND NOT EXISTS (" +
           "    SELECT b FROM Booking b " +
           "    WHERE b.timeSlot.id = ts.id " +
           "    AND b.status <> 'CANCELLED'" +
           ") " +
           "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsForCustomer(@Param("serviceId") Long serviceId, @Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts " +
           "WHERE ts.slotDate = :date " +
           "AND ts.isAvailable = true " +
           "AND NOT EXISTS (" +
           "    SELECT b FROM Booking b " +
           "    WHERE b.timeSlot.id = ts.id " +
           "    AND b.status <> 'CANCELLED'" +
           ") " +
           "ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsForCustomerByDate(@Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.service.id = :serviceId AND ts.slotDate BETWEEN :startDate AND :endDate ORDER BY ts.slotDate, ts.startTime")
    List<TimeSlot> findByServiceIdAndDateRange(@Param("serviceId") Long serviceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}