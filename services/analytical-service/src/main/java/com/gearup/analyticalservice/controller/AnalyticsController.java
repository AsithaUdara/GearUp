package com.gearup.analyticalservice.controller;

import com.gearup.analyticalservice.dto.*;
import com.gearup.analyticalservice.entity.CustomerAnalytics;
import com.gearup.analyticalservice.entity.PopularServices;
import com.gearup.analyticalservice.entity.RecentActivity;
import com.gearup.analyticalservice.entity.ServiceAnalytics;
import com.gearup.analyticalservice.repository.CustomerAnalyticsRepository;
import com.gearup.analyticalservice.repository.PopularServicesRepository;
import com.gearup.analyticalservice.repository.RecentActivityRepository;
import com.gearup.analyticalservice.repository.ServiceAnalyticsRepository;
import com.gearup.analyticalservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final ServiceAnalyticsRepository serviceAnalyticsRepository;
    private final PopularServicesRepository popularServicesRepository;
    private final RecentActivityRepository recentActivityRepository;
    private final CustomerAnalyticsRepository customerAnalyticsRepository;

    /**
     * GET /api/analytics/dashboard
     * Get complete dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsDashboardDTO> getDashboard() {
        log.info("GET /api/analytics/dashboard - Fetching complete dashboard data");
        AnalyticsDashboardDTO dashboard = analyticsService.getDashboardData();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET /api/analytics/metrics
     * Get dashboard metrics only
     */
    @GetMapping("/metrics")
    public ResponseEntity<DashboardMetricsDTO> getMetrics() {
        log.info("GET /api/analytics/metrics - Fetching dashboard metrics");
        DashboardMetricsDTO metrics = analyticsService.getDashboardMetrics();
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/analytics/appointments/trend
     * Get appointment trend data
     */
    @GetMapping("/appointments/trend")
    public ResponseEntity<List<AppointmentTrendDTO>> getAppointmentTrend(
            @RequestParam(defaultValue = "12") int weeks) {
        log.info("GET /api/analytics/appointments/trend - Fetching trend for {} weeks", weeks);
        List<AppointmentTrendDTO> trend = analyticsService.getAppointmentTrend(weeks);
        return ResponseEntity.ok(trend);
    }

    /**
     * GET /api/analytics/services/top
     * Get top services
     */
    @GetMapping("/services/top")
    public ResponseEntity<List<TopServiceDTO>> getTopServices(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("GET /api/analytics/services/top - Fetching top {} services", limit);
        List<TopServiceDTO> services = analyticsService.getTopServices(limit);
        return ResponseEntity.ok(services);
    }

    /**
     * GET /api/analytics/activities/recent
     * Get recent activities
     */
    @GetMapping("/activities/recent")
    public ResponseEntity<List<RecentActivityDTO>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/analytics/activities/recent - Fetching {} recent activities", limit);
        List<RecentActivityDTO> activities = analyticsService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    /**
     * GET /api/analytics/health
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analytics Service is running!");
    }

    // ==================== POST ENDPOINTS FOR ADDING MOCK DATA ====================

    /**
     * POST /api/analytics/services
     * Add new service analytics data
     */
    @PostMapping("/services")
    public ResponseEntity<ServiceAnalytics> addServiceAnalytics(@RequestBody CreateServiceAnalyticsRequest request) {
        log.info("POST /api/analytics/services - Adding service analytics: {}", request.getServiceName());
        
        ServiceAnalytics analytics = ServiceAnalytics.builder()
                .serviceName(request.getServiceName())
                .serviceType(request.getServiceType())
                .appointmentCount(request.getAppointmentCount())
                .totalRevenue(request.getTotalRevenue() != null ? BigDecimal.valueOf(request.getTotalRevenue()) : null)
                .averageRating(request.getAverageRating() != null ? BigDecimal.valueOf(request.getAverageRating()) : null)
                .recordDate(LocalDate.now())
                .build();
        
        ServiceAnalytics saved = serviceAnalyticsRepository.save(analytics);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/analytics/services/popular
     * Add a popular service
     */
    @PostMapping("/services/popular")
    public ResponseEntity<PopularServices> addPopularService(
            @RequestParam String serviceName,
            @RequestParam Integer bookingCount,
            @RequestParam(required = false) Double percentage,
            @RequestParam(required = false) Integer rank) {
        log.info("POST /api/analytics/services/popular - Adding: {}", serviceName);
        
        PopularServices service = PopularServices.builder()
                .serviceName(serviceName)
                .bookingCount(bookingCount)
                .percentageOfTotal(percentage)
                .rankPosition(rank)
                .build();
        
        PopularServices saved = popularServicesRepository.save(service);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/analytics/activities
     * Add a new recent activity
     */
    @PostMapping("/activities")
    public ResponseEntity<RecentActivity> addActivity(@RequestBody CreateRecentActivityRequest request) {
        log.info("POST /api/analytics/activities - Adding activity: {}", request.getEventDescription());
        
        RecentActivity activity = RecentActivity.builder()
                .eventDescription(request.getEventDescription())
                .status(request.getStatus())
                .eventTimestamp(LocalDateTime.now())
                .relatedEntityType(request.getRelatedEntityType())
                .relatedEntityId(request.getRelatedEntityId())
                .build();
        
        RecentActivity saved = recentActivityRepository.save(activity);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * POST /api/analytics/customers
     * Add customer analytics data
     */
    @PostMapping("/customers")
    public ResponseEntity<CustomerAnalytics> addCustomerAnalytics(
            @RequestParam Integer newCustomers,
            @RequestParam(required = false) Integer returningCustomers,
            @RequestParam(required = false) Integer totalCustomers,
            @RequestParam(required = false) Double retentionRate) {
        log.info("POST /api/analytics/customers - Adding customer data: {} new customers", newCustomers);
        
        CustomerAnalytics analytics = CustomerAnalytics.builder()
                .newCustomers(newCustomers)
                .returningCustomers(returningCustomers)
                .totalCustomers(totalCustomers)
                .customerRetentionRate(retentionRate)
                .recordDate(LocalDate.now())
                .build();
        
        CustomerAnalytics saved = customerAnalyticsRepository.save(analytics);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * DELETE /api/analytics/services/{id}
     * Delete service analytics by ID
     */
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteServiceAnalytics(@PathVariable Long id) {
        log.info("DELETE /api/analytics/services/{}", id);
        serviceAnalyticsRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/analytics/services/popular/{id}
     * Delete popular service by ID
     */
    @DeleteMapping("/services/popular/{id}")
    public ResponseEntity<Void> deletePopularService(@PathVariable Long id) {
        log.info("DELETE /api/analytics/services/popular/{}", id);
        popularServicesRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/analytics/activities/{id}
     * Delete activity by ID
     */
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        log.info("DELETE /api/analytics/activities/{}", id);
        recentActivityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/analytics/reset
     * Reset all data (clears everything)
     */
    @PostMapping("/reset")
    public ResponseEntity<String> resetAllData() {
        log.info("POST /api/analytics/reset - Clearing all data");
        serviceAnalyticsRepository.deleteAll();
        popularServicesRepository.deleteAll();
        recentActivityRepository.deleteAll();
        return ResponseEntity.ok("All analytics data has been cleared!");
    }
}
