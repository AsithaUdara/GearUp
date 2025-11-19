package com.gearup.templateservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "service_templates")
@Getter
@Setter
public class ServiceTemplate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 150, unique = true)
	private String name;

	@Column(length = 2000)
	private String description;

	@Column(precision = 12, scale = 2, nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private Integer durationMinutes;

	@Column(nullable = false)
	private Boolean active = Boolean.TRUE;

	@Column(updatable = false)
	private String createdBy;

	private String updatedBy;

	@Column(updatable = false)
	private Instant createdAt = Instant.now();

	private Instant updatedAt = Instant.now();
}
