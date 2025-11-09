package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.TimeSlotDTO;
import com.gearup.appointmentservice.entity.Service;
import com.gearup.appointmentservice.entity.TimeSlot;
import com.gearup.appointmentservice.repository.ServiceRepository;
import com.gearup.appointmentservice.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {
    
    private final TimeSlotRepository timeSlotRepository;
    private final ServiceRepository serviceRepository;
    
    public List<TimeSlotDTO> getAvailableTimeSlots(Long serviceId, LocalDate date) {
        log.debug("Fetching available time slots for service: {} on date: {}", serviceId, date);
        
        List<TimeSlot> timeSlots;
        if (serviceId != null) {
            timeSlots = timeSlotRepository.findAvailableSlotsByServiceAndDate(serviceId, date);
        } else {
            timeSlots = timeSlotRepository.findAvailableSlotsByDate(date);
        }
        
        return timeSlots.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    // For customer-facing requests: excludes slots with active bookings
    public List<TimeSlotDTO> getAvailableTimeSlotsForCustomer(Long serviceId, LocalDate date) {
        log.debug("Fetching truly available time slots (no active bookings) for service: {} on date: {}", serviceId, date);
        
        List<TimeSlot> timeSlots;
        if (serviceId != null) {
            timeSlots = timeSlotRepository.findAvailableSlotsForCustomer(serviceId, date);
        } else {
            timeSlots = timeSlotRepository.findAvailableSlotsForCustomerByDate(date);
        }
        
        return timeSlots.stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<TimeSlotDTO> getTimeSlotById(Long id) {
        log.debug("Fetching time slot with id: {}", id);
        return timeSlotRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    @Transactional
    public TimeSlotDTO createTimeSlot(TimeSlotDTO dto) {
        log.info("Creating time slot for service: {} on date: {}", dto.getServiceId(), dto.getSlotDate());
        
        Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found: " + dto.getServiceId()));
        
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setService(service);
        timeSlot.setSlotDate(dto.getSlotDate());
        timeSlot.setStartTime(dto.getStartTime());
        timeSlot.setEndTime(dto.getEndTime());
        timeSlot.setIsAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true);
        
        TimeSlot saved = timeSlotRepository.save(timeSlot);
        return convertToDTO(saved);
    }
    
    @Transactional
    public TimeSlotDTO updateAvailability(Long id, Boolean isAvailable) {
        log.info("Updating time slot {} availability to: {}", id, isAvailable);
        
        TimeSlot timeSlot = timeSlotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Time slot not found: " + id));
        
        timeSlot.setIsAvailable(isAvailable);
        TimeSlot updated = timeSlotRepository.save(timeSlot);
        
        return convertToDTO(updated);
    }
    
    @Transactional
    public void deleteTimeSlot(Long id) {
        log.info("Deleting time slot: {}", id);
        
        if (!timeSlotRepository.existsById(id)) {
            throw new RuntimeException("Time slot not found: " + id);
        }
        
        timeSlotRepository.deleteById(id);
    }
    
    @Transactional
    public void blockDateRange(Long serviceId, LocalDate startDate, LocalDate endDate) {
        log.info("Blocking time slots for service: {} from {} to {}", serviceId, startDate, endDate);
        
        List<TimeSlot> slotsToBlock = timeSlotRepository.findByServiceIdAndDateRange(serviceId, startDate, endDate);
        
        for (TimeSlot slot : slotsToBlock) {
            slot.setIsAvailable(false);
        }
        
        timeSlotRepository.saveAll(slotsToBlock);
        log.info("Blocked {} time slots", slotsToBlock.size());
    }
    
    @Transactional
    public void unblockDateRange(Long serviceId, LocalDate startDate, LocalDate endDate) {
        log.info("Unblocking time slots for service: {} from {} to {}", serviceId, startDate, endDate);
        
        List<TimeSlot> slotsToUnblock = timeSlotRepository.findByServiceIdAndDateRange(serviceId, startDate, endDate);
        
        for (TimeSlot slot : slotsToUnblock) {
            slot.setIsAvailable(true);
        }
        
        timeSlotRepository.saveAll(slotsToUnblock);
        log.info("Unblocked {} time slots", slotsToUnblock.size());
    }
    
    private TimeSlotDTO convertToDTO(TimeSlot timeSlot) {
        return new TimeSlotDTO(
                timeSlot.getId(),
                timeSlot.getService().getId(),
                timeSlot.getService().getName(),
                timeSlot.getSlotDate(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getIsAvailable()
        );
    }
}