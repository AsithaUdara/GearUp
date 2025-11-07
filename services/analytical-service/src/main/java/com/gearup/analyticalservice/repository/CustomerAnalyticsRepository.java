package com.gearup.analyticalservice.repository;

import com.gearup.analyticalservice.entity.CustomerAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CustomerAnalyticsRepository extends JpaRepository<CustomerAnalytics, Long> {
    
    Optional<CustomerAnalytics> findByRecordDate(LocalDate recordDate);
    
    // Use Spring Data JPA method naming convention instead of JPQL LIMIT
    Optional<CustomerAnalytics> findFirstByOrderByRecordDateDesc();
    
    @Query("SELECT SUM(ca.newCustomers) FROM CustomerAnalytics ca WHERE ca.recordDate BETWEEN :startDate AND :endDate")
    Integer getTotalNewCustomers(LocalDate startDate, LocalDate endDate);
}
