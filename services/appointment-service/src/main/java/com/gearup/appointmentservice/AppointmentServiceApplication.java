package com.gearup.appointmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

// TODO: Add @EnableScheduling for appointment reminder cron jobs
// TODO: Add @EnableAsync for asynchronous notification sending
// TODO: Add @EnableCaching for frequently accessed service/timeslot data
// TODO: Implement distributed lock for concurrent booking prevention
@SpringBootApplication
@ComponentScan(basePackages = "com.gearup")
public class AppointmentServiceApplication {
    // TODO: Add appointment lifecycle management (pending, confirmed, in-progress, completed, cancelled)
    // TODO: Implement appointment conflict detection across time slots
    // TODO: Add automatic appointment reminders (24h, 1h before)
    // TODO: Implement cancellation policies and penalties
    // TODO: Add waitlist functionality for fully booked slots
    // TODO: Implement recurring appointment support
    // TODO: Add calendar integration (Google Calendar, iCal export)
    // TODO: Implement no-show tracking and customer reliability scoring
    public static void main(String[] args) {
        SpringApplication.run(AppointmentServiceApplication.class, args);
    }
}
