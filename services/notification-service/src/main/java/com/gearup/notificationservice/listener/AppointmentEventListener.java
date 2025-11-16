package com.gearup.notificationservice.listener;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;
import com.gearup.shared.event.appointment.*;
import com.gearup.shared.messaging.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for appointment-related events and sends notifications
 */
//@Component  // Temporarily disabled - events handled by consolidated dispatcher
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_QUEUE)
    public void handleMessage(org.springframework.messaging.Message<?> message) {
        Object payload = message.getPayload();

        try {
            if (payload instanceof AppointmentCreatedEvent) {
                handleAppointmentCreated((AppointmentCreatedEvent) payload);
            } else if (payload instanceof EmployeeAssignedToAppointmentEvent) {
                handleEmployeeAssigned((EmployeeAssignedToAppointmentEvent) payload);
            } else if (payload instanceof AppointmentApprovedEvent) {
                handleAppointmentApproved((AppointmentApprovedEvent) payload);
            } else if (payload instanceof AppointmentCancelledEvent) {
                handleAppointmentCancelled((AppointmentCancelledEvent) payload);
            }
            // Silently ignore other event types
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", payload.getClass().getName(), e);
            throw e;
        }
    }

    private void handleAppointmentCreated(AppointmentCreatedEvent event) {
        try {
            log.info("🔔 Received AppointmentCreatedEvent: bookingId={}", event.getBookingId());

            // Notify customer
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Appointment Confirmed");
            notification.setMessage(String.format("Your appointment for %s has been confirmed. Booking ID: #%d",
                    event.getServiceName(), event.getBookingId()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getBookingId().toString());
            notification.setRelatedEntityType("BOOKING");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent appointment confirmation notification to customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send appointment created notification for booking: {}", event.getBookingId(), e);
        }
    }

    private void handleEmployeeAssigned(EmployeeAssignedToAppointmentEvent event) {
        try {
            log.info("🔔 Received EmployeeAssignedToAppointmentEvent: bookingId={}, employeeId={}",
                    event.getBookingId(), event.getEmployeeId());

            // Notify customer about employee assignment
            NotificationRequest customerNotification = new NotificationRequest();
            customerNotification.setUserId(event.getCustomerId());
            customerNotification.setTitle("Technician Assigned");
            customerNotification.setMessage(String.format("%s has been assigned to your %s appointment. Scheduled for: %s",
                    event.getEmployeeName(), event.getServiceName(), event.getAppointmentDateTime()));
            customerNotification.setType(NotificationType.GENERAL);
            customerNotification.setPriority(NotificationPriority.MEDIUM);
            customerNotification.setRelatedEntityId(event.getBookingId().toString());
            customerNotification.setRelatedEntityType("BOOKING");
            customerNotification.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");

            notificationService.createNotification(customerNotification);

            // Notify employee about new assignment
            NotificationRequest employeeNotification = new NotificationRequest();
            employeeNotification.setUserId(event.getEmployeeId().toString());
            employeeNotification.setTitle("New Task Assigned");
            employeeNotification.setMessage(String.format("You have been assigned to %s for customer %s. Time slot: %s",
                    event.getServiceName(), event.getCustomerName(), event.getTimeSlot()));
            employeeNotification.setType(NotificationType.TASK_ASSIGNED);
            employeeNotification.setPriority(NotificationPriority.HIGH);
            employeeNotification.setRelatedEntityId(event.getBookingId().toString());
            employeeNotification.setRelatedEntityType("BOOKING");
            employeeNotification.setDeliveryChannels("[\"WEB\", \"PUSH\"]");

            notificationService.createNotification(employeeNotification);

            log.info("✅ Sent employee assignment notifications - Customer: {}, Employee: {}",
                    event.getCustomerName(), event.getEmployeeName());

        } catch (Exception e) {
            log.error("❌ Failed to send employee assignment notifications for booking: {}", event.getBookingId(), e);
        }
    }

    private void handleAppointmentApproved(AppointmentApprovedEvent event) {
        try {
            log.info("🔔 Received AppointmentApprovedEvent: bookingId={}", event.getBookingId());

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Appointment Approved");
            notification.setMessage(String.format("Your %s appointment (Booking #%d) has been approved by management.",
                    event.getServiceName(), event.getBookingId()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.MEDIUM);
            notification.setRelatedEntityId(event.getBookingId().toString());
            notification.setRelatedEntityType("BOOKING");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent appointment approval notification to customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send appointment approval notification for booking: {}", event.getBookingId(), e);
        }
    }

    private void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        try {
            log.info("🔔 Received AppointmentCancelledEvent: bookingId={}", event.getBookingId());

            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(event.getCustomerId());
            notification.setTitle("Appointment Cancelled");
            notification.setMessage(String.format("Your %s appointment (Booking #%d) has been cancelled. Reason: %s",
                    event.getServiceName(), event.getBookingId(), event.getCancellationReason()));
            notification.setType(NotificationType.GENERAL);
            notification.setPriority(NotificationPriority.HIGH);
            notification.setRelatedEntityId(event.getBookingId().toString());
            notification.setRelatedEntityType("BOOKING");
            notification.setDeliveryChannels("[\"WEB\", \"EMAIL\", \"SMS\"]");

            notificationService.createNotification(notification);
            log.info("✅ Sent appointment cancellation notification to customer: {}", event.getCustomerName());

        } catch (Exception e) {
            log.error("❌ Failed to send appointment cancellation notification for booking: {}", event.getBookingId(), e);
        }
    }
}
