package com.gearup.chatbotservice.loader;

import com.gearup.chatbotservice.service.DocumentIndexerService;
import com.gearup.chatbotservice.service.DocumentIndexerService.DocumentData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Knowledge Base Loader
 * Loads initial knowledge base documents on application startup
 */
@Component
@ConditionalOnProperty(name = "knowledge-base.loader.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseLoader implements ApplicationRunner {
    
    private final DocumentIndexerService indexer;
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Starting Knowledge Base Loading ===");
        
        try {
            loadServiceInformation();
            loadPolicies();
            loadFAQs();
            loadBusinessInformation();
            
            log.info("=== Knowledge Base Loading Completed Successfully ===");
        } catch (Exception e) {
            log.error("Error loading knowledge base", e);
        }
    }
    
    /**
     * Load service catalog information
     */
    private void loadServiceInformation() {
        log.info("Loading service information...");
        
        List<DocumentData> services = new ArrayList<>();
        
        // Service 1: Oil Change
        services.add(new DocumentData(
            """
            Oil Change & Filter Service
            Price: LKR 5,000 - 8,000 (depending on vehicle type)
            Duration: 30-45 minutes
            Description: Complete oil change service with premium filter replacement.
            Includes: Engine oil drain, new oil filter installation, refill with quality engine oil,
            basic under-hood inspection, and fluid level check.
            Recommended every 5,000-7,500 km or 6 months.
            """,
            Map.of("type", "service", "service_id", "oil_change"),
            "service_catalog",
            "maintenance"
        ));
        
        // Service 2: Full Service
        services.add(new DocumentData(
            """
            Full Service Package
            Price: LKR 15,000 - 25,000 (depending on vehicle type)
            Duration: 2-3 hours
            Description: Comprehensive vehicle inspection and maintenance package.
            Includes: Engine oil and filter change, brake inspection and adjustment, 
            fluid top-up (brake, coolant, windshield), tire pressure check and adjustment,
            battery test, air filter inspection, full diagnostic scan, 
            suspension check, and detailed service report.
            Recommended every 10,000 km or 12 months.
            """,
            Map.of("type", "service", "service_id", "full_service"),
            "service_catalog",
            "maintenance"
        ));
        
        // Service 3: Brake Service
        services.add(new DocumentData(
            """
            Brake Service & Repair
            Price: LKR 8,000 - 20,000 (depending on parts needed)
            Duration: 1-2 hours
            Description: Complete brake system inspection and service.
            Includes: Brake pad inspection and replacement, brake fluid check and replacement,
            rotor inspection and resurfacing, caliper inspection, brake line check,
            parking brake adjustment, and brake performance test.
            Recommended every 30,000 km or when brake warning light appears.
            """,
            Map.of("type", "service", "service_id", "brake_service"),
            "service_catalog",
            "repair"
        ));
        
        // Service 4: Battery Service
        services.add(new DocumentData(
            """
            Battery Check & Replacement
            Price: LKR 3,000 (check) / LKR 12,000-25,000 (with replacement)
            Duration: 30 minutes - 1 hour
            Description: Battery health check and replacement service.
            Includes: Battery voltage test, load test, terminal cleaning,
            connection check, charging system test, and battery replacement if needed.
            Most batteries last 3-5 years. We stock major brands.
            """,
            Map.of("type", "service", "service_id", "battery_service"),
            "service_catalog",
            "electrical"
        ));
        
        // Service 5: AC Service
        services.add(new DocumentData(
            """
            Air Conditioning Service
            Price: LKR 5,000 - 15,000 (depending on issue)
            Duration: 1-2 hours
            Description: AC system inspection and repair service.
            Includes: Refrigerant level check and recharge, AC compressor inspection,
            condenser and evaporator check, cabin filter replacement,
            leak detection, and cooling performance test.
            Recommended annually before summer season.
            """,
            Map.of("type", "service", "service_id", "ac_service"),
            "service_catalog",
            "comfort"
        ));
        
        indexer.indexDocuments(services);
        log.info("Loaded {} service documents", services.size());
    }
    
    /**
     * Load company policies
     */
    private void loadPolicies() {
        log.info("Loading company policies...");
        
        List<DocumentData> policies = new ArrayList<>();
        
        // Cancellation Policy
        policies.add(new DocumentData(
            """
            GearUp Cancellation and Rescheduling Policy:
            
            Free Cancellation:
            - Cancel or reschedule free of charge up to 24 hours before your scheduled appointment
            - Can be done through our mobile app, website, or by calling customer service
            
            Late Cancellation:
            - Cancellations within 24 hours but more than 6 hours before appointment: 25% fee
            - Cancellations within 6 hours of appointment: 50% cancellation fee
            
            No-Shows:
            - Full service amount will be charged for no-shows
            - This helps us serve other customers who need the time slot
            
            Rescheduling:
            - You can reschedule your appointment free of charge up to 12 hours before
            - Subject to availability
            - Easy rescheduling through our app or website
            
            Emergency Situations:
            - We understand emergencies happen
            - Contact us directly for special consideration
            """,
            Map.of("type", "policy", "policy_id", "cancellation"),
            "company_policies",
            "policy"
        ));
        
        // Payment Policy
        policies.add(new DocumentData(
            """
            GearUp Payment Information and Policy:
            
            Accepted Payment Methods:
            - Cash (LKR)
            - Credit Cards: Visa, Mastercard, American Express
            - Debit Cards: All major Sri Lankan banks
            - Digital Wallets: frimi, iPay, eZ Cash
            - Bank Transfers (with prior arrangement)
            
            Payment Timing:
            - Payment is due after service completion
            - Inspection of work before payment
            - Detailed itemized invoice provided
            
            Pricing:
            - All prices quoted include parts and labor
            - Additional parts may be required (we'll inform you first)
            - Prices may vary based on vehicle make and model
            - Free quotes provided before work begins
            
            Insurance:
            - We accept all major vehicle insurance
            - Direct billing available for approved insurance companies
            - Required documentation: Insurance card, claim number
            
            Warranty:
            - 30-day warranty on all services
            - 6-month warranty on parts we install
            - Keep your service receipt for warranty claims
            """,
            Map.of("type", "policy", "policy_id", "payment"),
            "company_policies",
            "policy"
        ));
        
        indexer.indexDocuments(policies);
        log.info("Loaded {} policy documents", policies.size());
    }
    
    /**
     * Load frequently asked questions
     */
    private void loadFAQs() {
        log.info("Loading FAQs...");
        
        List<DocumentData> faqs = new ArrayList<>();
        
        faqs.add(new DocumentData(
            """
            Q: What are your business hours?
            A: We are open Monday to Saturday, 8:00 AM to 7:00 PM.
            We are closed on Sundays and public holidays.
            Extended hours available by appointment for fleet customers.
            
            Q: Do I need an appointment?
            A: While walk-ins are welcome, we highly recommend booking an appointment
            to ensure availability and minimize wait time. Appointments can be booked through:
            - Our mobile app (fastest)
            - Our website
            - Phone call: [PHONE NUMBER]
            - WhatsApp: [WHATSAPP NUMBER]
            
            Q: How long does a typical service take?
            A: Service duration varies:
            - Oil change: 30-45 minutes
            - Full service: 2-3 hours
            - Brake service: 1-2 hours
            - Diagnostic check: 45 minutes - 1 hour
            We'll give you an accurate time estimate when you book.
            
            Q: Can I wait while my vehicle is serviced?
            A: Yes! We have a comfortable waiting lounge with:
            - Free WiFi
            - Refreshments
            - TV and magazines
            - Charging stations
            Alternatively, we can drop you at a nearby location and call when ready.
            
            Q: What if you find additional problems during service?
            A: We will always contact you before performing any additional work.
            You'll receive:
            - Detailed explanation of the issue
            - Cost estimate for the repair
            - Your approval before proceeding
            No surprise charges - ever!
            """,
            Map.of("type", "faq", "category", "general"),
            "faq",
            "information"
        ));
        
        indexer.indexDocuments(faqs);
        log.info("Loaded {} FAQ documents", faqs.size());
    }
    
    /**
     * Load general business information
     */
    private void loadBusinessInformation() {
        log.info("Loading business information...");
        
        List<DocumentData> businessInfo = new ArrayList<>();
        
        businessInfo.add(new DocumentData(
            """
            About GearUp Auto Service:
            
            GearUp is a modern vehicle service and maintenance platform serving Sri Lanka.
            We combine traditional expertise with modern technology to provide 
            convenient, transparent, and reliable vehicle care.
            
            Our Services:
            - Scheduled maintenance
            - Repairs and diagnostics
            - Emergency services
            - Fleet management
            - Pre-purchase inspections
            
            Why Choose GearUp:
            - Certified and experienced technicians
            - Modern diagnostic equipment
            - Transparent pricing
            - Quality parts and materials
            - Convenient online booking
            - Service reminders
            - Digital service history
            - Customer satisfaction guarantee
            
            Our Locations:
            - Colombo Main Branch
            - Kandy Service Center
            - Galle Workshop
            (More locations coming soon!)
            
            Contact Us:
            - Customer Service: [PHONE]
            - Email: support@gearup.lk
            - Website: www.gearup.lk
            - Mobile App: Available on iOS and Android
            """,
            Map.of("type", "about", "category", "company"),
            "company_info",
            "information"
        ));
        
        indexer.indexDocuments(businessInfo);
        log.info("Loaded {} business information documents", businessInfo.size());
    }
}
