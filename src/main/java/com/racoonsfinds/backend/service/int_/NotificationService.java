package com.racoonsfinds.backend.service.int_;

import java.util.List;

import com.racoonsfinds.backend.model.Notification;

public interface NotificationService {
    Notification createNotification(Long userId, String title, String message);
    List<Notification> getNotificationsByUser(Long userId);
    void markAsRead(Long notificationId, Long userId);
}