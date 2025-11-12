package com.gearup.modificationservice.service;

import com.gearup.modificationservice.dto.ModificationServiceDTO;
import com.gearup.modificationservice.entity.ModificationService;
import com.gearup.modificationservice.repository.ModificationServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModificationServiceService {
    
    private final ModificationServiceRepository modificationServiceRepository;
    
    public List<ModificationServiceDTO> getAllActiveServices() {
        log.debug("Fetching all active modification services");
        List<ModificationService> services = modificationServiceRepository.findActiveServicesOrderByName();
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<ModificationServiceDTO> getServiceById(Long id) {
        log.debug("Fetching modification service with id: {}", id);
        return modificationServiceRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    private ModificationServiceDTO convertToDTO(ModificationService service) {
        return new ModificationServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getBasePrice(),
                service.getEstimatedDurationHours(),
                service.getActive()
        );
    }
}