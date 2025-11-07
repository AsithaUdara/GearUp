package com.gearup.chatbotservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Action Handler Service
 * Handles specific actions based on user intent (booking, status check, modifications)
 */
@Service
@ConditionalOnProperty(name = "rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class ActionHandlerService {
    
    private final RabbitTemplate rabbitTemplate;
    private final OllamaService ollamaService;
    
    /**
     * Handle action based on classified intent
     * 
     * @param intent classified intent
     * @param message user message
     * @param userId user identifier
     * @return response message
     */
    public String handleAction(String intent, String message, String userId) {
        
        log.info("Handling action for intent '{}' from user {}", intent, userId);
        
        return switch (intent) {
            case "booking" -> handleBookingIntent(message, userId);
            case "status" -> handleStatusIntent(message, userId);
            case "modify" -> handleModifyIntent(message, userId);
            case "pricing" -> handlePricingIntent(message, userId);
            case "services" -> handleServicesIntent(message, userId);
            default -> handleGeneralIntent(message);
        };
    }
    
    /**
     * Handle booking intent - guide user through booking process
     */
    private String handleBookingIntent(String message, String userId) {
        log.debug("Handling booking intent");
        
        // Extract booking details using LLM
        String extractionPrompt = String.format("""
            Extract booking details from this message: "%s"
            
            Respond in this format:
            Service: [service type or "Not specified"]
            Date: [preferred date or "Not specified"]
            Time: [preferred time or "Not specified"]
            Vehicle: [vehicle info or "Not specified"]
            
            If any detail is not mentioned, write "Not specified"
            Be concise.
            """, message);
        
        String extracted = ollamaService.generateResponse(extractionPrompt, "");
        
        // Check if enough information is provided
        long notSpecifiedCount = extracted.lines()
            .filter(line -> line.contains("Not specified"))
            .count();
        
        if (notSpecifiedCount >= 3) {
            return buildBookingGatheringResponse(extracted);
        }
        
        // TODO: Publish event to booking service via RabbitMQ
        // For now, provide confirmation
        return String.format("""
            Great! I've noted your booking request with the following details:
            
            %s
            
            Our team will contact you shortly to confirm your appointment.
            You can also complete your booking directly through our app or website.
            
            Is there anything else I can help you with?
            """, extracted);
    }
    
    /**
     * Handle status check intent
     */
    private String handleStatusIntent(String message, String userId) {
        log.debug("Handling status check intent");
        
        // TODO: Query booking service for user's bookings
        // For now, provide template response
        
        return """
            To check your booking status, I'll need some information:
            
            1. Your booking reference number, OR
            2. The phone number you used for the booking
            
            You can also check your booking status in:
            - Our mobile app under 'My Bookings'
            - Our website after logging in
            - By calling our customer service: [PHONE_NUMBER]
            
            Please provide your booking reference or phone number, and I'll help you track your service.
            """;
    }
    
    /**
     * Handle modification intent
     */
    private String handleModifyIntent(String message, String userId) {
        log.debug("Handling modification intent");
        
        return """
            I can help you modify your booking. What would you like to change?
            
            Options:
            1. Reschedule to a different date/time
            2. Change the service type
            3. Cancel the booking
            4. Update vehicle information
            
            Please provide:
            - Your booking reference number
            - What you'd like to modify
            
            Note: Changes must be made at least 24 hours before your scheduled appointment.
            """;
    }
    
    /**
     * Handle pricing inquiries
     */
    private String handlePricingIntent(String message, String userId) {
        log.debug("Handling pricing intent");
        
        // This should be answered by RAG with knowledge base
        return """
            I'd be happy to help with pricing information!
            
            Our service pricing varies depending on:
            - Type of service
            - Vehicle make and model
            - Additional parts or repairs needed
            
            Could you please specify which service you're interested in? For example:
            - Oil change
            - Full service
            - Brake service
            - Engine diagnostics
            - Tire replacement
            
            Or you can browse all our services and pricing on our website.
            """;
    }
    
    /**
     * Handle services inquiry
     */
    private String handleServicesIntent(String message, String userId) {
        log.debug("Handling services intent");
        
        return """
            We offer a comprehensive range of vehicle services:
            
            🔧 Maintenance Services:
            - Oil change and filter replacement
            - Full service package
            - Brake inspection and service
            - Battery check and replacement
            
            🔍 Diagnostic Services:
            - Engine diagnostics
            - Computer diagnostics
            - Pre-purchase inspection
            
            🛠️ Repair Services:
            - Engine repairs
            - Transmission service
            - Suspension and steering
            - Air conditioning service
            
            Which service are you interested in learning more about?
            """;
    }
    
    /**
     * Handle general information requests
     */
    private String handleGeneralIntent(String message) {
        return "I can help you with that. Could you provide more details about what you're looking for?";
    }
    
    /**
     * Build response for gathering more booking information
     */
    private String buildBookingGatheringResponse(String currentInfo) {
        return String.format("""
            I'd be happy to help you book a service! I need a few more details:
            
            Current information:
            %s
            
            Please provide:
            1. What type of service do you need? (e.g., oil change, full service)
            2. When would you like to schedule it? (preferred date and time)
            3. What vehicle will you be bringing in? (make, model, year)
            
            Once I have these details, I'll help you complete your booking!
            """, currentInfo);
    }
    
    /**
     * Publish booking event to RabbitMQ
     * TODO: Implement when booking service integration is ready
     */
    private void publishBookingEvent(String userId, Map<String, String> bookingDetails) {
        // Example structure:
        // rabbitTemplate.convertAndSend(
        //     "booking.exchange",
        //     "booking.create.request",
        //     bookingDetails
        // );
    }
}
