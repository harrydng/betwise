package com.betwise.service;

import com.betwise.dto.NotificationResponse;
import com.betwise.dto.UnreadCountResponse;
import com.betwise.exception.ResourceNotFoundException;
import com.betwise.model.Notification;
import com.betwise.model.User;
import com.betwise.repository.NotificationRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // MARK: - Reading

    public List<NotificationResponse> getNotifications(User user) {

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::new)
                .toList();
    }

    public UnreadCountResponse getUnreadCount(User user) {

        return new UnreadCountResponse(
                notificationRepository.countByUserIdAndReadAtIsNull(
                        user.getId()));
    }

    @Transactional
    public NotificationResponse markAsRead(
            User user,
            Long notificationId) {

        Notification notification = notificationRepository
                .findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found"));

        if (!notification.isRead()) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }

        return new NotificationResponse(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {

        LocalDateTime now = LocalDateTime.now();

        List<Notification> unread = notificationRepository
                .findByUserIdAndReadAtIsNull(user.getId());

        unread.forEach(notification -> notification.setReadAt(now));

        notificationRepository.saveAll(unread);
    }

    // MARK: - Creating
    //
    // Called by other services when something worth telling the user
    // about happens. They run inside the caller's transaction, so a
    // notification is only saved if the action itself succeeds.

    public void notifyTradeFilled(
            User user,
            Long transactionId,
            boolean isBuy,
            String ticker,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal totalAmount) {

        String body = String.format(
                "%s %s %s at %s for %s.",
                isBuy ? "Bought" : "Sold",
                quantity.stripTrailingZeros().toPlainString(),
                ticker,
                formatMoney(price),
                formatMoney(totalAmount));

        save(user, Notification.NotificationType.TRADE_FILLED, "Order filled", body, transactionId);
    }

    public void notifyCashTransfer(
            User user,
            Long destinationPortfolioId,
            BigDecimal amount,
            String sourceName,
            String destinationName) {

        String body = String.format(
                "Moved %s from %s to %s.",
                formatMoney(amount),
                sourceName,
                destinationName);

        save(user, Notification.NotificationType.CASH_TRANSFER, "Cash transferred", body, destinationPortfolioId);
    }

    public void notifyLessonReward(
            User user,
            Long lessonId,
            String lessonTitle,
            BigDecimal reward) {

        String body = String.format(
                "You earned %s for finishing \"%s\". It's been added to your buying power.",
                formatMoney(reward),
                lessonTitle);

        save(user, Notification.NotificationType.LESSON_REWARD, "Lesson complete", body, lessonId);
    }

    public void notifyQuizCompleted(
            User user,
            Long quizId,
            String quizTitle,
            int score) {

        String body = String.format(
                "You scored %d%% on \"%s\".",
                score,
                quizTitle);

        save(user, Notification.NotificationType.QUIZ_COMPLETED, "Quiz complete", body, quizId);
    }

    private void save(
            User user,
            Notification.NotificationType type,
            String title,
            String body,
            Long relatedId) {

        notificationRepository.save(
                new Notification(user, type, title, body, relatedId));
    }

    private static String formatMoney(BigDecimal amount) {
        return NumberFormat
                .getCurrencyInstance(Locale.US)
                .format(amount);
    }
}
