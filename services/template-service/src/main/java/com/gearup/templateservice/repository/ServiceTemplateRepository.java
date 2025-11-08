package com.gearup.templateservice.repository;

import com.gearup.templateservice.model.ServiceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceTemplateRepository extends JpaRepository<ServiceTemplate, Long> {
    List<ServiceTemplate> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
