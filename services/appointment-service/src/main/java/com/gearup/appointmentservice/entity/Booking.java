package com.gearup.appointmentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -----------------------------
    // RELATIONSHIPS
    // -----------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    // -----------------------------
    // BASIC CUSTOMER INFO
    // -----------------------------
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    // -----------------------------
    // STATUS & NOTES
    // -----------------------------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // -----------------------------
    // NEW: EMPLOYEE ASSIGNMENT
    // -----------------------------
    @Column(name = "assigned_employee_id")
    private Long assignedEmployeeId;
    // In the future you can replace this with:
    // @ManyToOne
    // @JoinColumn(name = "assigned_employee_id")
    // private Employee assignedEmployee;

    // -----------------------------
    // TIMESTAMPS
    // -----------------------------
    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}