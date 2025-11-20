package com.gearup.templateservice.repository;

import com.gearup.templateservice.model.ServiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceTemplateRepository extends JpaRepository<ServiceTemplate, Long> {
    List<ServiceTemplate> findByActiveTrue();
    org.springframework.data.domain.Page<ServiceTemplate> findByActiveTrue(org.springframework.data.domain.Pageable pageable);
    boolean existsByNameIgnoreCase(String name);
}
