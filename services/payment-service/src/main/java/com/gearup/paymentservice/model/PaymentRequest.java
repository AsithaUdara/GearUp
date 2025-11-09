package com.gearup.paymentservice.model;

import com.gearup.paymentservice.enums.PaymentRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_name", nullable = false)
    private String customerName;
    
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;
    
    @Column(name = "vehicle_info", nullable = false, length = 500)
    private String vehicleInfo;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentRequestStatus status;
    
    @Column(name = "submitted_by", nullable = false)
    private String submittedBy;
    
    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;
    
    @Column(name = "approved_date")
    private LocalDate approvedDate;
    
    @Column(name = "rejected_date")
    private LocalDate rejectedDate;
    
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    
    @OneToMany(mappedBy = "paymentRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ServiceItem> serviceItems = new ArrayList<>();
    
    @OneToOne(mappedBy = "paymentRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CustomerBill customerBill;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
    public void addServiceItem(ServiceItem serviceItem) {
        serviceItems.add(serviceItem);
        serviceItem.setPaymentRequest(this);
    }
    
    public void removeServiceItem(ServiceItem serviceItem) {
        serviceItems.remove(serviceItem);
        serviceItem.setPaymentRequest(null);
    }
}
