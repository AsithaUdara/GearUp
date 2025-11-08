package com.gearup.analyticalservice.repository;

import com.gearup.analyticalservice.entity.ServiceAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceAnalyticsRepository extends JpaRepository<ServiceAnalytics, Long> {
    
    @Query("SELECT sa FROM ServiceAnalytics sa WHERE sa.recordDate BETWEEN :startDate AND :endDate ORDER BY sa.recordDate ASC")
    List<ServiceAnalytics> findByDateRange(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT SUM(sa.appointmentCount) FROM ServiceAnalytics sa WHERE sa.recordDate = :date")
    Integer getTotalAppointmentsByDate(LocalDate date);
    
    @Query("SELECT sa FROM ServiceAnalytics sa WHERE sa.recordDate BETWEEN :startDate AND :endDate GROUP BY sa.recordDate ORDER BY sa.recordDate ASC")
    List<ServiceAnalytics> getAppointmentTrend(LocalDate startDate, LocalDate endDate);
}
