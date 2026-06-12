package com.racoonsfinds.backend.notification.service;

import java.util.List;

import com.racoonsfinds.backend.notification.domain.Notification;

public interface NotificationService {
    Notification createNotification(Long userId, String title, String message);
    List<Notification> getNotificationsByUser(Long userId);
    void markAsRead(Long notificationId, Long userId);
}