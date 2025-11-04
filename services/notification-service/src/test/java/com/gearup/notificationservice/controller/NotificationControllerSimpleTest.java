package com.gearup.notificationservice.controller;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.dto.NotificationResponse;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.service.NotificationService;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Tests")
class NotificationControllerSimpleTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationRequest testRequest;
    private NotificationResponse testResponse;
    private final String TEST_USER_ID = "test-user-123";
    private final Long TEST_NOTIFICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        testRequest = NotificationRequest.builder()
                .userId(TEST_USER_ID)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.INVOICE_CREATED)
                .priority(NotificationPriority.MEDIUM)
                .relatedEntityId("invoice-123")
                .relatedEntityType("INVOICE")
                .actionUrl("/invoices/123")
                .build();

        testResponse = NotificationResponse.builder()
                .id(TEST_NOTIFICATION_ID)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.INVOICE_CREATED)
                .priority(NotificationPriority.MEDIUM)
                .isRead(false)
                .relatedEntityId("invoice-123")
                .relatedEntityType("INVOICE")
                .actionUrl("/invoices/123")
                .createdAt(LocalDateTime.now())
                .build();

        // Use lenient stubbing for authentication as it's not used in all tests
        lenient().when(authentication.getName()).thenReturn(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should create notification successfully")
    void shouldCreateNotificationSuccessfully() {
        // Arrange
        when(notificationService.createNotification(any(NotificationRequest.class)))
                .thenReturn(testResponse);

        // Act
        ResponseEntity<NotificationResponse> response = notificationController.createNotification(testRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(TEST_NOTIFICATION_ID);
        assertThat(response.getBody().getTitle()).isEqualTo("Test Notification");
        assertThat(response.getBody().getIsRead()).isFalse();
        
        verify(notificationService, times(1)).createNotification(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void shouldMarkNotificationAsReadSuccessfully() {
        // Arrange
        NotificationResponse readResponse = NotificationResponse.builder()
                .id(TEST_NOTIFICATION_ID)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.INVOICE_CREATED)
                .priority(NotificationPriority.MEDIUM)
                .isRead(true)
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationService.markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID))
                .thenReturn(readResponse);

        // Act
        ResponseEntity<NotificationResponse> response = notificationController
                .markAsRead(TEST_NOTIFICATION_ID, authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getIsRead()).isTrue();
        assertThat(response.getBody().getReadAt()).isNotNull();
        
        verify(notificationService, times(1)).markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("Should delete notification successfully")
    void shouldDeleteNotificationSuccessfully() {
        // Act
        ResponseEntity<Void> response = notificationController.deleteNotification(TEST_NOTIFICATION_ID, authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService, times(1)).deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void shouldMarkAllNotificationsAsRead() {
        // Act
        ResponseEntity<Void> response = notificationController.markAllAsRead(authentication);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(notificationService, times(1)).markAllAsRead(TEST_USER_ID);
    }
}
