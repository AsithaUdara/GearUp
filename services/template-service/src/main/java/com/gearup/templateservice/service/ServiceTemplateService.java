package com.gearup.templateservice.service;

import com.gearup.templateservice.dto.ServiceTemplateDto;
import com.gearup.templateservice.exception.ResourceNotFoundException;
import com.gearup.templateservice.model.ServiceTemplate;
import com.gearup.templateservice.repository.ServiceTemplateRepository;
import com.gearup.templateservice.messaging.TemplateEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceTemplateService {

    private final ServiceTemplateRepository repository;
    private final TemplateEventPublisher eventPublisher;

    @Cacheable(value = "activeTemplates", condition = "#onlyActive == true")
    public List<ServiceTemplateDto> findAll(boolean onlyActive) {
        var list = onlyActive ? repository.findByActiveTrue() : repository.findAll();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Cacheable(value = "serviceTemplates", key = "#id")
    public ServiceTemplateDto findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }

    @Transactional
    @CacheEvict(value = {"serviceTemplates", "activeTemplates"}, allEntries = true)
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
        var createdDto = toDto(saved);
        // Publish event (non-critical; swallow exceptions to avoid failing request)
        safePublishCreated(createdDto, adminUid);
        return createdDto;
    }

    @Transactional
    @CachePut(value = "serviceTemplates", key = "#id")
    @CacheEvict(value = "activeTemplates", allEntries = true)
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
        var updatedDto = toDto(saved);
        safePublishUpdated(updatedDto, adminUid);
        return updatedDto;
    }

    @Transactional
    @CacheEvict(value = {"serviceTemplates", "activeTemplates"}, allEntries = true)
    public void delete(Long id, String adminUid) {
        var entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        repository.delete(entity);
        safePublishDeleted(entity.getId(), entity.getName(), adminUid);
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

    private void safePublishCreated(ServiceTemplateDto dto, String adminUid) {
        try { eventPublisher.publishCreated(dto, adminUid != null ? adminUid : "system"); } catch (Exception ignored) {}
    }
    private void safePublishUpdated(ServiceTemplateDto dto, String adminUid) {
        try { eventPublisher.publishUpdated(dto, adminUid != null ? adminUid : "system"); } catch (Exception ignored) {}
    }
    private void safePublishDeleted(Long id, String name, String adminUid) {
        try { eventPublisher.publishDeleted(id, name, adminUid != null ? adminUid : "system"); } catch (Exception ignored) {}
    }
}
