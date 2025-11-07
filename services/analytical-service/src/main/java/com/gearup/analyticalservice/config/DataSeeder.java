package com.gearup.analyticalservice.config;

import com.gearup.analyticalservice.entity.*;
import com.gearup.analyticalservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final ServiceAnalyticsRepository serviceAnalyticsRepository;
    private final CustomerAnalyticsRepository customerAnalyticsRepository;
    private final PopularServicesRepository popularServicesRepository;
    private final RecentActivityRepository recentActivityRepository;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Check if data already exists
            if (serviceAnalyticsRepository.count() > 0) {
                log.info("Database already contains data. Skipping seeding.");
                return;
            }

            log.info("Starting database seeding with mock analytics data...");

            seedServiceAnalytics();
            seedCustomerAnalytics();
            seedPopularServices();
            seedRecentActivities();

            log.info("Database seeding completed successfully!");
        };
    }

    private void seedServiceAnalytics() {
        log.info("Seeding service analytics data...");
        
        List<ServiceAnalytics> analytics = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random random = new Random();

        String[] services = {"Oil Change", "Brake Service", "Tire Rotation", "Battery Check", "Diagnostics"};
        
        // Generate data for last 12 weeks (matching the trend in UI)
        for (int week = 0; week < 12; week++) {
            LocalDate weekDate = today.minusWeeks(11 - week);
            
            for (String service : services) {
                // Generate increasing trend matching the UI chart
                int baseCount = 15 + week * 2; // Gradually increasing
                int variance = random.nextInt(5);
                int appointmentCount = baseCount + variance;
                
                analytics.add(ServiceAnalytics.builder()
                        .serviceName(service)
                        .serviceType("MAINTENANCE")
                        .appointmentCount(appointmentCount)
                        .totalRevenue(BigDecimal.valueOf(appointmentCount * (80 + random.nextInt(120))))
                        .averageRating(BigDecimal.valueOf(4.0 + random.nextDouble()))
                        .recordDate(weekDate)
                        .build());
            }
        }

        serviceAnalyticsRepository.saveAll(analytics);
        log.info("Seeded {} service analytics records", analytics.size());
    }

    private void seedCustomerAnalytics() {
        log.info("Seeding customer analytics data...");
        
        List<CustomerAnalytics> analytics = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // ⭐ CHANGE THESE VALUES TO WHATEVER YOU WANT ⭐
        int newCustomers = 340;          // ← CHANGE THIS: Number of new customers
        int returningCustomers = 245;    // ← CHANGE THIS: Returning customers
        int totalCustomers = 545;        // ← CHANGE THIS: Total customers
        double retentionRate = 82.35;    // ← CHANGE THIS: Retention rate percentage
        
        // Add customer data with your custom values
        analytics.add(CustomerAnalytics.builder()
                .newCustomers(newCustomers)
                .returningCustomers(returningCustomers)
                .totalCustomers(totalCustomers)
                .customerRetentionRate(retentionRate)
                .recordDate(today)
                .build());

        customerAnalyticsRepository.saveAll(analytics);
        log.info("Seeded customer analytics: {} new customers", newCustomers);
    }

    private void seedPopularServices() {
        log.info("Seeding popular services data...");
        
        List<PopularServices> services = new ArrayList<>();

        // ⭐ CHANGE THESE VALUES TO ADD/MODIFY SERVICES ⭐
        // Format: serviceName, bookingCount, percentage, rank
        
        services.add(PopularServices.builder()
                .serviceName("Oil Change")        // ← CHANGE THIS
                .bookingCount(126)                // ← CHANGE THIS
                .percentageOfTotal(36.8)          // ← CHANGE THIS
                .rankPosition(1)
                .build());

        services.add(PopularServices.builder()
                .serviceName("Brake Service")     // ← CHANGE THIS
                .bookingCount(94)                 // ← CHANGE THIS
                .percentageOfTotal(27.5)          // ← CHANGE THIS
                .rankPosition(2)
                .build());

        services.add(PopularServices.builder()
                .serviceName("Tire Rotation")     // ← CHANGE THIS
                .bookingCount(83)                 // ← CHANGE THIS
                .percentageOfTotal(24.3)          // ← CHANGE THIS
                .rankPosition(3)
                .build());

        services.add(PopularServices.builder()
                .serviceName("Battery Check")     // ← CHANGE THIS
                .bookingCount(57)                 // ← CHANGE THIS
                .percentageOfTotal(16.7)          // ← CHANGE THIS
                .rankPosition(4)
                .build());

        services.add(PopularServices.builder()
                .serviceName("Diagnostics")       // ← CHANGE THIS
                .bookingCount(41)                 // ← CHANGE THIS
                .percentageOfTotal(12.0)          // ← CHANGE THIS
                .rankPosition(5)
                .build());

        // ⭐ ADD MORE SERVICES HERE IF YOU WANT ⭐
        // services.add(PopularServices.builder()
        //         .serviceName("Engine Repair")
        //         .bookingCount(35)
        //         .percentageOfTotal(10.2)
        //         .rankPosition(6)
        //         .build());

        popularServicesRepository.saveAll(services);
        log.info("Seeded {} popular services", services.size());
    }

    private void seedRecentActivities() {
        log.info("Seeding recent activities (matching UI)...");
        
        List<RecentActivity> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // Matching the exact activities from your UI screenshot
        activities.add(RecentActivity.builder()
                .eventDescription("Appointment confirmed: BMW 3 Series")
                .status("OK")
                .eventTimestamp(now.minusMinutes(2))
                .relatedEntityType("APPOINTMENT")
                .relatedEntityId("APT-2024-001")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Inventory low: Oil Filter (OF-67890)")
                .status("ATTENTION")
                .eventTimestamp(now.minusMinutes(15))
                .relatedEntityType("INVENTORY")
                .relatedEntityId("OF-67890")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Service completed: Honda Civic")
                .status("OK")
                .eventTimestamp(now.minusMinutes(28))
                .relatedEntityType("SERVICE")
                .relatedEntityId("SVC-2024-045")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("New customer registered: John Smith")
                .status("OK")
                .eventTimestamp(now.minusMinutes(45))
                .relatedEntityType("CUSTOMER")
                .relatedEntityId("CUST-2024-097")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Payment received: Toyota Camry service")
                .status("OK")
                .eventTimestamp(now.minusHours(1).minusMinutes(12))
                .relatedEntityType("PAYMENT")
                .relatedEntityId("PAY-2024-134")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Maintenance due: Mercedes-Benz E-Class")
                .status("ATTENTION")
                .eventTimestamp(now.minusHours(2).minusMinutes(5))
                .relatedEntityType("MAINTENANCE")
                .relatedEntityId("VEH-2024-023")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Appointment rescheduled: Audi A4")
                .status("OK")
                .eventTimestamp(now.minusHours(3).minusMinutes(30))
                .relatedEntityType("APPOINTMENT")
                .relatedEntityId("APT-2024-002")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Quality check passed: Ford F-150")
                .status("OK")
                .eventTimestamp(now.minusHours(4).minusMinutes(20))
                .relatedEntityType("SERVICE")
                .relatedEntityId("SVC-2024-046")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Inventory restocked: Brake Pads")
                .status("OK")
                .eventTimestamp(now.minusHours(5).minusMinutes(45))
                .relatedEntityType("INVENTORY")
                .relatedEntityId("BP-45678")
                .build());

        activities.add(RecentActivity.builder()
                .eventDescription("Emergency service: Chevrolet Silverado")
                .status("WARNING")
                .eventTimestamp(now.minusHours(6).minusMinutes(10))
                .relatedEntityType("SERVICE")
                .relatedEntityId("SVC-2024-047")
                .build());

        recentActivityRepository.saveAll(activities);
        log.info("Seeded {} recent activities", activities.size());
    }
}
