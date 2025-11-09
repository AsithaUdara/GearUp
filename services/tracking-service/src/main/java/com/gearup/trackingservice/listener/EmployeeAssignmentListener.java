package com.gearup.trackingservice.listener;

import com.gearup.shared.event.appointment.EmployeeAssignedToAppointmentEvent;
import com.gearup.shared.messaging.RabbitMQConstants;
import com.gearup.trackingservice.dto.request.CreateTaskRequest;
import com.gearup.trackingservice.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * CRITICAL LISTENER: Listens for employee assignment events and auto-creates work tasks
 * This is the core of the event-driven employee-to-task assignment workflow
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeAssignmentListener {
    
    private final TaskService taskService;
    
    /**
     * Handles EmployeeAssignedToAppointmentEvent
     * When an employee is assigned to an appointment, automatically create a work task
     * 
     * Queue: tracking.queue
     * Exchange: appointment.exchange
     * Routing Key: appointment.employee.assigned
     */
    @RabbitListener(queues = RabbitMQConstants.TRACKING_QUEUE)
    public void handleEmployeeAssigned(EmployeeAssignedToAppointmentEvent event) {
        try {
            log.info("🎯 Received EmployeeAssignedToAppointmentEvent: bookingId={}, employeeId={}, employeeName={}", 
                    event.getBookingId(), event.getEmployeeId(), event.getEmployeeName());
            
            // Create task request from event
            CreateTaskRequest taskRequest = CreateTaskRequest.builder()
                    .serviceId(event.getServiceId().toString())
                    .vehicle("N/A")  // Vehicle info not available in event - can be enhanced
                    .customer(event.getCustomerName())
                    .serviceType(event.getServiceName())
                    .assigneeId(event.getEmployeeId().toString())
                    .estimatedDuration(60)  // Default 60 minutes - can be enhanced
                    .notes(String.format("Auto-created from appointment #%d. Scheduled: %s. Time slot: %s",
                            event.getBookingId(),
                            event.getAppointmentDateTime(),
                            event.getTimeSlot()))
                    .build();
            
            // Create the task
            var taskResponse = taskService.createTask(taskRequest);
            
            log.info("✅ Successfully created task {} for employee {} based on appointment #{}", 
                    taskResponse.getTaskId(), event.getEmployeeName(), event.getBookingId());
            
        } catch (Exception e) {
            log.error("❌ Failed to create task for EmployeeAssignedToAppointmentEvent: bookingId={}, employeeId={}", 
                    event.getBookingId(), event.getEmployeeId(), e);
            // In production, you might want to republish to DLQ or retry
        }
    }
}
