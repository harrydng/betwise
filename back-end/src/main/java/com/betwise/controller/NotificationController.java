package com.betwise.controller;

import com.betwise.dto.NotificationResponse;
import com.betwise.dto.UnreadCountResponse;

import com.betwise.model.User;
import com.betwise.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.getNotifications(user)
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.getUnreadCount(user)
        );
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long id
    ) {

        User user =
                (User) authentication.getPrincipal();

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        user,
                        id
                )
        );
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication
    ) {

        User user =
                (User) authentication.getPrincipal();

        notificationService.markAllAsRead(user);

        return ResponseEntity
                .noContent()
                .build();
    }
}
