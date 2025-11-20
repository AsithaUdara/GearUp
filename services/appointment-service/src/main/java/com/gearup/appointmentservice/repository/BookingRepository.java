package com.gearup.appointmentservice.repository;

import com.gearup.appointmentservice.entity.Booking;
import com.gearup.appointmentservice.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByUserId(String userId);
    
    List<Booking> findByUserIdOrderByBookingDateDesc(String userId);
    
    List<Booking> findByStatus(BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = :status ORDER BY b.bookingDate DESC")
    List<Booking> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") BookingStatus status);
    
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.timeSlot.id = :timeSlotId AND b.status != 'CANCELLED'")
    boolean existsByTimeSlotIdAndStatusNotCancelled(@Param("timeSlotId") Long timeSlotId);
    
    List<Booking> findByAssignedEmployeeId(Long employeeId);
}