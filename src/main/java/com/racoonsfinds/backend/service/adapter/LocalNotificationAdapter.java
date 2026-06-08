package com.racoonsfinds.backend.service.adapter;

import org.springframework.stereotype.Component;

import com.racoonsfinds.backend.service.int_.NotificationService;
import com.racoonsfinds.backend.service.port.NotificationPort;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador local: llama a NotificationService directamente.
 * Reemplazar por KafkaNotificationAdapter al extraer notification-service.
 */
@Component
@RequiredArgsConstructor
public class LocalNotificationAdapter implements NotificationPort {

    private final NotificationService notificationService;

    @Override
    public void notifyUser(Long userId, String title, String message) {
        notificationService.createNotification(userId, title, message);
    }
}
