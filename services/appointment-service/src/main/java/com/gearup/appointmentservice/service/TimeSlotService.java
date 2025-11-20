package com.gearup.appointmentservice.service;

import com.gearup.appointmentservice.dto.TimeSlotDTO;
import com.gearup.appointmentservice.entity.TimeSlot;
import com.gearup.appointmentservice.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimeSlotService {
    
    private final TimeSlotRepository timeSlotRepository;
    
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
    
    public List<TimeSlotDTO> getAvailableTimeSlotsForCustomer(Long serviceId, LocalDate date) {
        return getAvailableTimeSlots(serviceId, date);
    }
    
    public Optional<TimeSlotDTO> getTimeSlotById(Long id) {
        log.debug("Fetching time slot with id: {}", id);
        return timeSlotRepository.findById(id)
                .map(this::convertToDTO);
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