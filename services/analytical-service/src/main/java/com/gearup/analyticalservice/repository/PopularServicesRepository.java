package com.gearup.analyticalservice.repository;

import com.gearup.analyticalservice.entity.PopularServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularServicesRepository extends JpaRepository<PopularServices, Long> {
    
    @Query("SELECT ps FROM PopularServices ps ORDER BY ps.bookingCount DESC")
    List<PopularServices> findTopServices();
    
    @Query("SELECT ps FROM PopularServices ps ORDER BY ps.rankPosition ASC")
    List<PopularServices> findAllOrderedByRank();
}
