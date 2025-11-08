package com.gearup.templateservice.service;

import com.gearup.templateservice.dto.ServiceTemplateDto;
import com.gearup.templateservice.exception.ResourceNotFoundException;
import com.gearup.templateservice.model.ServiceTemplate;
import com.gearup.templateservice.repository.ServiceTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTemplateService {

    private final ServiceTemplateRepository repository;
    private final com.gearup.templateservice.messaging.TemplateEventPublisher eventPublisher;

    public List<ServiceTemplateDto> findAll(boolean onlyActive) {
        var list = onlyActive ? repository.findByActiveTrue() : repository.findAll();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    public ServiceTemplateDto findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }

    @Transactional
    public ServiceTemplateDto create(ServiceTemplateDto dto, String adminUid) {
        if (repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("A template with this name already exists");
        }
        var entity = new ServiceTemplate();
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationMinutes());
        entity.setActive(dto.getActive() != null ? dto.getActive() : Boolean.TRUE);
        entity.setCreatedBy(adminUid);
        entity.setUpdatedBy(adminUid);
        var saved = repository.save(entity);
        // Publish domain event
        eventPublisher.publishCreated(saved, adminUid);
        return toDto(saved);
    }

    @Transactional
    public ServiceTemplateDto update(Long id, ServiceTemplateDto dto, String adminUid) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));

        if (!entity.getName().equalsIgnoreCase(dto.getName()) && repository.existsByNameIgnoreCase(dto.getName())) {
            throw new IllegalArgumentException("A template with this name already exists");
        }
        entity.setName(dto.getName().trim());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice());
        entity.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getActive() != null) entity.setActive(dto.getActive());
        entity.setUpdatedBy(adminUid);
        var saved = repository.save(entity);
        eventPublisher.publishUpdated(saved, adminUid);
        return toDto(saved);
    }

    @Transactional
    public void delete(Long id, String adminUid) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        repository.delete(entity);
        eventPublisher.publishDeleted(entity, adminUid);
    }

    private ServiceTemplateDto toDto(ServiceTemplate e) {
        return ServiceTemplateDto.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .price(e.getPrice())
                .durationMinutes(e.getDurationMinutes())
                .active(e.getActive())
                .build();
    }
}
