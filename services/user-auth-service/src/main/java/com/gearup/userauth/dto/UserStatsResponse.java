package com.gearup.userauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {
    
    private long totalUsers;
    private long activeUsers;
    private long deactivatedUsers;
    private long totalCustomers;
    private long totalEmployees;
    private long totalAdmins;
    private long newUsersThisMonth;
    private long newUsersToday;
    
    // Role distribution
    private Map<String, Long> usersByRole;
    
    // Status distribution
    private Map<String, Long> usersByStatus;
}
