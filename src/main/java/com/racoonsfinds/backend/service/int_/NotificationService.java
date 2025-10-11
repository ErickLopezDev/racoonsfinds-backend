package com.racoonsfinds.backend.service.int_;

import com.racoonsfinds.backend.model.Notification;

public interface NotificationService {
    Notification createNotification(Long userId, String title, String message);
}