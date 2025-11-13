package com.gearup.userauth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "session_token", nullable = false, unique = true, length = 500)
    private String sessionToken;
    
    @Column(name = "refresh_token", unique = true, length = 500)
    private String refreshToken;
    
    @Column(name = "device_info", columnDefinition = "TEXT")
    private String deviceInfo;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "refresh_expires_at")
    private LocalDateTime refreshExpiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastAccessedAt = LocalDateTime.now();
    }
}
