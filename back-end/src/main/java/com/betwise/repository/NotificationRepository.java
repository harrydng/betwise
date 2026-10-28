package com.betwise.repository;

import com.betwise.model.Notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadAtIsNull(Long userId);

    Optional<Notification> findByIdAndUserId(
            Long id,
            Long userId
    );

    long countByUserIdAndReadAtIsNull(Long userId);

    void deleteByUserId(Long userId);
}
