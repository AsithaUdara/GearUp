package com.gearup.appointmentservice.repository;

import com.gearup.appointmentservice.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    
    List<TimeSlot> findByServiceIdAndSlotDateAndIsAvailableTrue(Long serviceId, LocalDate slotDate);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.service.id = :serviceId AND ts.slotDate = :date AND ts.isAvailable = true ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsByServiceAndDate(@Param("serviceId") Long serviceId, @Param("date") LocalDate date);
    
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.slotDate = :date AND ts.isAvailable = true ORDER BY ts.startTime")
    List<TimeSlot> findAvailableSlotsByDate(@Param("date") LocalDate date);
}