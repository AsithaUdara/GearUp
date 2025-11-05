package com.gearup.notificationservice.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.gearup.notificationservice.dto.NotificationRequest;
import com.gearup.notificationservice.dto.NotificationResponse;
import com.gearup.notificationservice.dto.UnreadCountResponse;
import com.gearup.notificationservice.entity.Notification;
import com.gearup.notificationservice.entity.NotificationPriority;
import com.gearup.notificationservice.entity.NotificationType;
import com.gearup.notificationservice.repository.NotificationRepository;
import com.gearup.shared.cache.RedisNotificationCache;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RedisNotificationCache redisNotificationCache;

    @Mock
    private WebSocketNotificationSender webSocketNotificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private NotificationRequest testRequest;
    private final String TEST_USER_ID = "test-user-123";
    private final Long TEST_NOTIFICATION_ID = 1L;

    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .id(TEST_NOTIFICATION_ID)
                .userId(TEST_USER_ID)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.INVOICE_CREATED)
                .priority(NotificationPriority.MEDIUM)
                .isRead(false)
                .relatedEntityId("invoice-123")
                .relatedEntityType("INVOICE")
                .actionUrl("/invoices/123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

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
    }

    @Nested
    @DisplayName("Create Notification Tests")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should create notification successfully")
        void shouldCreateNotificationSuccessfully() {
            // Arrange
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationResponse response = notificationService.createNotification(testRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(TEST_NOTIFICATION_ID);
            assertThat(response.getTitle()).isEqualTo("Test Notification");
            assertThat(response.getMessage()).isEqualTo("This is a test notification");
            assertThat(response.getType()).isEqualTo(NotificationType.INVOICE_CREATED);
            assertThat(response.getPriority()).isEqualTo(NotificationPriority.MEDIUM);
            assertThat(response.getIsRead()).isFalse();

            verify(notificationRepository).save(any(Notification.class));
            verify(redisNotificationCache).incrementUnreadCount(TEST_USER_ID);
            verify(webSocketNotificationSender).sendToUser(eq(TEST_USER_ID), any(NotificationResponse.class));
        }

        @Test
        @DisplayName("Should create notification with high priority")
        void shouldCreateHighPriorityNotification() {
            // Arrange
            testRequest.setPriority(NotificationPriority.HIGH);
            testNotification.setPriority(NotificationPriority.HIGH);
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationResponse response = notificationService.createNotification(testRequest);

            // Assert
            assertThat(response.getPriority()).isEqualTo(NotificationPriority.HIGH);
            verify(notificationRepository).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should create notification without optional fields")
        void shouldCreateNotificationWithoutOptionalFields() {
            // Arrange
            testRequest.setRelatedEntityId(null);
            testRequest.setRelatedEntityType(null);
            testRequest.setActionUrl(null);
            testNotification.setRelatedEntityId(null);
            testNotification.setRelatedEntityType(null);
            testNotification.setActionUrl(null);
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationResponse response = notificationService.createNotification(testRequest);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getRelatedEntityId()).isNull();
            assertThat(response.getRelatedEntityType()).isNull();
            assertThat(response.getActionUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("Get User Notifications Tests")
    class GetUserNotificationsTests {

        @Test
        @DisplayName("Should get paginated notifications for user")
        void shouldGetPaginatedNotifications() {
            // Arrange
            int page = 0;
            int size = 20;
            Pageable pageable = PageRequest.of(page, size);
            
            List<Notification> notifications = Arrays.asList(
                testNotification,
                Notification.builder()
                    .id(2L)
                    .userId(TEST_USER_ID)
                    .title("Second Notification")
                    .message("Second message")
                    .type(NotificationType.TASK_ASSIGNED)
                    .priority(NotificationPriority.LOW)
                    .isRead(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            
            Page<Notification> notificationPage = new PageImpl<>(notifications, pageable, notifications.size());
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(TEST_USER_ID, pageable))
                .thenReturn(notificationPage);

            // Act
            Page<NotificationResponse> response = notificationService.getUserNotifications(TEST_USER_ID, page, size);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
            assertThat(response.getNumber()).isEqualTo(page);
            verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(TEST_USER_ID, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no notifications exist")
        void shouldReturnEmptyPageWhenNoNotifications() {
            // Arrange
            int page = 0;
            int size = 20;
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(TEST_USER_ID, pageable))
                .thenReturn(emptyPage);

            // Act
            Page<NotificationResponse> response = notificationService.getUserNotifications(TEST_USER_ID, page, size);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Get Unread Notifications Tests")
    class GetUnreadNotificationsTests {

        @Test
        @DisplayName("Should get all unread notifications for user")
        void shouldGetUnreadNotifications() {
            // Arrange
            List<Notification> unreadNotifications = Arrays.asList(
                testNotification,
                Notification.builder()
                    .id(2L)
                    .userId(TEST_USER_ID)
                    .title("Second Unread")
                    .message("Another unread")
                    .type(NotificationType.TASK_ASSIGNED)
                    .priority(NotificationPriority.HIGH)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
            );
            
            when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(unreadNotifications);

            // Act
            List<NotificationResponse> response = notificationService.getUnreadNotifications(TEST_USER_ID);

            // Assert
            assertThat(response).hasSize(2);
            assertThat(response).allMatch(n -> !n.getIsRead());
            verify(notificationRepository).findByUserIdAndIsReadFalseOrderByCreatedAtDesc(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should return empty list when no unread notifications")
        void shouldReturnEmptyListWhenNoUnreadNotifications() {
            // Arrange
            when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(TEST_USER_ID))
                .thenReturn(List.of());

            // Act
            List<NotificationResponse> response = notificationService.getUnreadNotifications(TEST_USER_ID);

            // Assert
            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Unread Count Tests")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should get unread count from cache")
        void shouldGetUnreadCountFromCache() {
            // Arrange
            Long cachedCount = 5L;
            when(redisNotificationCache.getUnreadCount(TEST_USER_ID)).thenReturn(cachedCount);

            // Act
            UnreadCountResponse response = notificationService.getUnreadCount(TEST_USER_ID);

            // Assert
            assertThat(response.getUnreadCount()).isEqualTo(cachedCount);
            verify(redisNotificationCache).getUnreadCount(TEST_USER_ID);
            verify(notificationRepository, never()).countByUserIdAndIsReadFalse(anyString());
        }

        @Test
        @DisplayName("Should get unread count from database when not in cache")
        void shouldGetUnreadCountFromDatabaseWhenNotInCache() {
            // Arrange
            long dbCount = 3L;
            when(redisNotificationCache.getUnreadCount(TEST_USER_ID)).thenReturn(null);
            when(notificationRepository.countByUserIdAndIsReadFalse(TEST_USER_ID)).thenReturn(dbCount);

            // Act
            UnreadCountResponse response = notificationService.getUnreadCount(TEST_USER_ID);

            // Assert
            assertThat(response.getUnreadCount()).isEqualTo(dbCount);
            verify(redisNotificationCache).getUnreadCount(TEST_USER_ID);
            verify(notificationRepository).countByUserIdAndIsReadFalse(TEST_USER_ID);
            verify(redisNotificationCache).setUnreadCount(TEST_USER_ID, dbCount);
        }

        @Test
        @DisplayName("Should return zero count when no unread notifications")
        void shouldReturnZeroCountWhenNoUnreadNotifications() {
            // Arrange
            when(redisNotificationCache.getUnreadCount(TEST_USER_ID)).thenReturn(null);
            when(notificationRepository.countByUserIdAndIsReadFalse(TEST_USER_ID)).thenReturn(0L);

            // Act
            UnreadCountResponse response = notificationService.getUnreadCount(TEST_USER_ID);

            // Assert
            assertThat(response.getUnreadCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Mark As Read Tests")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkNotificationAsRead() {
            // Arrange
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

            // Act
            NotificationResponse response = notificationService.markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);

            // Assert
            assertThat(response).isNotNull();
            assertThat(testNotification.getIsRead()).isTrue();
            assertThat(testNotification.getReadAt()).isNotNull();
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository).save(testNotification);
            verify(redisNotificationCache).decrementUnreadCount(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should not update if already read")
        void shouldNotUpdateIfAlreadyRead() {
            // Arrange
            testNotification.setIsRead(true);
            testNotification.setReadAt(LocalDateTime.now());
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

            // Act
            NotificationResponse response = notificationService.markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID);

            // Assert
            assertThat(response).isNotNull();
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository, never()).save(any(Notification.class));
            verify(redisNotificationCache, never()).decrementUnreadCount(anyString());
        }

        @Test
        @DisplayName("Should throw exception when notification not found")
        void shouldThrowExceptionWhenNotificationNotFound() {
            // Arrange
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> notificationService.markAsRead(TEST_NOTIFICATION_ID, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
            
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository, never()).save(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception when user unauthorized")
        void shouldThrowExceptionWhenUserUnauthorized() {
            // Arrange
            String differentUserId = "different-user-456";
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.markAsRead(TEST_NOTIFICATION_ID, differentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized access");
            
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository, never()).save(any(Notification.class));
        }
    }

    @Nested
    @DisplayName("Mark All As Read Tests")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all notifications as read")
        void shouldMarkAllNotificationsAsRead() {
            // Arrange
            int updatedCount = 5;
            when(notificationRepository.markAllAsReadByUserId(TEST_USER_ID)).thenReturn(updatedCount);

            // Act
            int result = notificationService.markAllAsRead(TEST_USER_ID);

            // Assert
            assertThat(result).isEqualTo(updatedCount);
            verify(notificationRepository).markAllAsReadByUserId(TEST_USER_ID);
            verify(redisNotificationCache).setUnreadCount(TEST_USER_ID, 0L);
        }

        @Test
        @DisplayName("Should handle when no notifications to mark as read")
        void shouldHandleWhenNoNotificationsToMarkAsRead() {
            // Arrange
            when(notificationRepository.markAllAsReadByUserId(TEST_USER_ID)).thenReturn(0);

            // Act
            int result = notificationService.markAllAsRead(TEST_USER_ID);

            // Assert
            assertThat(result).isZero();
            verify(notificationRepository).markAllAsReadByUserId(TEST_USER_ID);
            verify(redisNotificationCache).setUnreadCount(TEST_USER_ID, 0L);
        }
    }

    @Nested
    @DisplayName("Delete Notification Tests")
    class DeleteNotificationTests {

        @Test
        @DisplayName("Should delete notification successfully")
        void shouldDeleteNotificationSuccessfully() {
            // Arrange
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

            // Act
            notificationService.deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);

            // Assert
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository).delete(testNotification);
            verify(redisNotificationCache).decrementUnreadCount(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should not decrement cache if notification was already read")
        void shouldNotDecrementCacheIfAlreadyRead() {
            // Arrange
            testNotification.setIsRead(true);
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

            // Act
            notificationService.deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID);

            // Assert
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository).delete(testNotification);
            verify(redisNotificationCache, never()).decrementUnreadCount(anyString());
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent notification")
        void shouldThrowExceptionWhenDeletingNonExistentNotification() {
            // Arrange
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> notificationService.deleteNotification(TEST_NOTIFICATION_ID, TEST_USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Notification not found");
            
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository, never()).delete(any(Notification.class));
        }

        @Test
        @DisplayName("Should throw exception when unauthorized user tries to delete")
        void shouldThrowExceptionWhenUnauthorizedUserTriesToDelete() {
            // Arrange
            String differentUserId = "different-user-456";
            when(notificationRepository.findById(TEST_NOTIFICATION_ID)).thenReturn(Optional.of(testNotification));

            // Act & Assert
            assertThatThrownBy(() -> notificationService.deleteNotification(TEST_NOTIFICATION_ID, differentUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unauthorized access");
            
            verify(notificationRepository).findById(TEST_NOTIFICATION_ID);
            verify(notificationRepository, never()).delete(any(Notification.class));
        }
    }
}
