package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.ServiceDTO;
import com.gearup.appointmentservice.entity.Service;
import com.gearup.appointmentservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceService {
    
    private final ServiceRepository serviceRepository;
    
    public List<ServiceDTO> getAllActiveServices() {
        log.debug("Fetching all active services");
        List<Service> services = serviceRepository.findActiveServicesOrderByName();
        return services.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<ServiceDTO> getServiceById(Long id) {
        log.debug("Fetching service with id: {}", id);
        return serviceRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    private ServiceDTO convertToDTO(Service service) {
        return new ServiceDTO(
                service.getId(),
                service.getName(),
                service.getDescription(),
                service.getDurationMinutes(),
                service.getPrice(),
                service.getIsActive()
        );
    }
}