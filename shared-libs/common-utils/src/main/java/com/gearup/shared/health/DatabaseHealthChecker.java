package com.gearup.shared.health;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Database health checker for monitoring database connectivity.
 * Only active when DataSource is available.
 */
@Component
public class DatabaseHealthChecker implements HealthIndicator {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseHealthChecker.class);
    private final DataSource dataSource;
    
    public DatabaseHealthChecker(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                logger.debug("Database connection is healthy");
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "UP")
                    .build();
            }
        } catch (SQLException e) {
            logger.error("Database connection failed: {}", e.getMessage());
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("database", "PostgreSQL")
                .build();
        }
        
        return Health.down().withDetail("database", "Connection validation failed").build();
    }
}
