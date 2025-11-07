package com.gearup.chatbotservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for IntentClassifierService
 */
@DisplayName("IntentClassifierService Tests")
class IntentClassifierServiceTest {

    private IntentClassifierService service;

    @BeforeEach
    void setUp() {
        service = new IntentClassifierService();
    }

    @Test
    @DisplayName("Should classify booking intent")
    void testBookingIntent() {
        // When
        String intent1 = service.classifyIntent("I want to book an appointment");
        String intent2 = service.classifyIntent("Can I schedule a service?");
        String intent3 = service.classifyIntent("Need to make an appointment");

        // Then
        assertThat(intent1).isEqualTo("booking");
        assertThat(intent2).isEqualTo("booking");
        assertThat(intent3).isEqualTo("booking");
    }

    @Test
    @DisplayName("Should classify pricing intent")
    void testPricingIntent() {
        // When
        String intent1 = service.classifyIntent("How much does it cost?");
        String intent2 = service.classifyIntent("What is the price?");
        String intent3 = service.classifyIntent("Tell me about your fees");

        // Then
        assertThat(intent1).isEqualTo("pricing");
        assertThat(intent2).isEqualTo("pricing");
        assertThat(intent3).isEqualTo("pricing");
    }

    @Test
    @DisplayName("Should classify status intent")
    void testStatusIntent() {
        // When
        String intent1 = service.classifyIntent("What is the status of my booking?");
        String intent2 = service.classifyIntent("Check my appointment progress");
        String intent3 = service.classifyIntent("What is my booking status?");

        // Then
        assertThat(intent1).isEqualTo("status");
        assertThat(intent2).isEqualTo("status");
        assertThat(intent3).isEqualTo("status");
    }

    @Test
    @DisplayName("Should classify modify intent")
    void testModifyIntent() {
        // When
        String intent1 = service.classifyIntent("I need to change my appointment");
        String intent2 = service.classifyIntent("Can I cancel my booking?");
        String intent3 = service.classifyIntent("Want to reschedule my service");

        // Then
        assertThat(intent1).isEqualTo("modify");
        assertThat(intent2).isEqualTo("modify");
        assertThat(intent3).isEqualTo("modify");
    }

    @Test
    @DisplayName("Should classify services intent")
    void testServicesIntent() {
        // When
        String intent1 = service.classifyIntent("What services do you offer?");
        String intent2 = service.classifyIntent("Which services are available?");
        String intent3 = service.classifyIntent("Do you provide oil change?");

        // Then
        assertThat(intent1).isEqualTo("services");
        assertThat(intent2).isEqualTo("services");
        assertThat(intent3).isEqualTo("services");
    }

    @Test
    @DisplayName("Should classify hours intent")
    void testHoursIntent() {
        // When
        String intent1 = service.classifyIntent("What are your business hours?");
        String intent2 = service.classifyIntent("When are you open?");
        String intent3 = service.classifyIntent("What time do you close?");

        // Then
        assertThat(intent1).isEqualTo("hours");
        assertThat(intent2).isEqualTo("hours");
        assertThat(intent3).isEqualTo("hours");
    }

    @Test
    @DisplayName("Should classify location intent")
    void testLocationIntent() {
        // When
        String intent1 = service.classifyIntent("Where are you located?");
        String intent2 = service.classifyIntent("What is your address?");
        String intent3 = service.classifyIntent("How do I find you?");

        // Then
        assertThat(intent1).isEqualTo("location");
        assertThat(intent2).isEqualTo("location");
        assertThat(intent3).isEqualTo("location");
    }

    @Test
    @DisplayName("Should classify greeting intent")
    void testGreetingIntent() {
        // When
        String intent1 = service.classifyIntent("Hello");
        String intent2 = service.classifyIntent("Hi there");
        String intent3 = service.classifyIntent("Good morning");

        // Then
        assertThat(intent1).isEqualTo("greeting");
        assertThat(intent2).isEqualTo("greeting");
        assertThat(intent3).isEqualTo("greeting");
    }

    @Test
    @DisplayName("Should default to information for unrecognized intents")
    void testDefaultIntent() {
        // When
        String intent1 = service.classifyIntent("Random text that doesn't match any pattern");
        String intent2 = service.classifyIntent("Tell me about the weather");

        // Then
        assertThat(intent1).isEqualTo("information");
        assertThat(intent2).isEqualTo("information");
    }

    @Test
    @DisplayName("Should handle null message")
    void testNullMessage() {
        // When
        String intent = service.classifyIntent(null);

        // Then
        assertThat(intent).isEqualTo("information");
    }

    @Test
    @DisplayName("Should handle empty message")
    void testEmptyMessage() {
        // When
        String intent = service.classifyIntent("");

        // Then
        assertThat(intent).isEqualTo("information");
    }

    @Test
    @DisplayName("Should handle blank message")
    void testBlankMessage() {
        // When
        String intent = service.classifyIntent("   ");

        // Then
        assertThat(intent).isEqualTo("information");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void testCaseInsensitive() {
        // When
        String intent1 = service.classifyIntent("BOOK AN APPOINTMENT");
        String intent2 = service.classifyIntent("book an appointment");
        String intent3 = service.classifyIntent("BoOk An ApPoInTmEnT");

        // Then
        assertThat(intent1).isEqualTo("booking");
        assertThat(intent2).isEqualTo("booking");
        assertThat(intent3).isEqualTo("booking");
    }
}
