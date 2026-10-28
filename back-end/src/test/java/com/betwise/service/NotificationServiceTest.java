package com.betwise.service;

import com.betwise.dto.NotificationResponse;
import com.betwise.exception.ResourceNotFoundException;
import com.betwise.model.Notification;
import com.betwise.model.User;
import com.betwise.repository.NotificationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("harry@gmail.com");
    }

    @Test
    void tradeFilledShouldDescribeTheOrder() {

        notificationService.notifyTradeFilled(
                user,
                42L,
                true,
                "AAPL",
                new BigDecimal("2.000000"),
                new BigDecimal("200.00"),
                new BigDecimal("400.00"));

        ArgumentCaptor<Notification> captor =
                ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertEquals(Notification.NotificationType.TRADE_FILLED, saved.getType());
        assertEquals("Order filled", saved.getTitle());
        assertEquals("Bought 2 AAPL at $200.00 for $400.00.", saved.getBody());
        assertEquals(42L, saved.getRelatedId());
        assertFalse(saved.isRead());
    }

    @Test
    void markAsReadShouldSetReadAt() {

        Notification notification = new Notification(
                user,
                Notification.NotificationType.LESSON_REWARD,
                "Lesson complete",
                "body",
                5L);

        when(notificationRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(notification));

        NotificationResponse response =
                notificationService.markAsRead(user, 10L);

        assertTrue(response.isRead());
        assertNotNull(notification.getReadAt());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadShouldFailForAnotherUsersNotification() {

        when(notificationRepository.findByIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(user, 10L));

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsReadShouldMarkEveryUnreadNotification() {

        Notification first = new Notification(
                user, Notification.NotificationType.TRADE_FILLED, "a", "a", 1L);
        Notification second = new Notification(
                user, Notification.NotificationType.CASH_TRANSFER, "b", "b", 2L);

        when(notificationRepository.findByUserIdAndReadAtIsNull(1L))
                .thenReturn(List.of(first, second));

        notificationService.markAllAsRead(user);

        assertTrue(first.isRead());
        assertTrue(second.isRead());
        verify(notificationRepository).saveAll(List.of(first, second));
    }
}
