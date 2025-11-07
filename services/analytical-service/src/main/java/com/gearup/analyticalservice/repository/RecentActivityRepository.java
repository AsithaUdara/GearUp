package com.gearup.analyticalservice.repository;

import com.gearup.analyticalservice.entity.RecentActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecentActivityRepository extends JpaRepository<RecentActivity, Long> {
    
    @Query("SELECT ra FROM RecentActivity ra ORDER BY ra.eventTimestamp DESC LIMIT 20")
    List<RecentActivity> findRecentActivities();
}
