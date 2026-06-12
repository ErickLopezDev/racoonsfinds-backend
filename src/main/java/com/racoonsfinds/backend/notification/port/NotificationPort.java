package com.racoonsfinds.backend.notification.port;

/**
 * Puerto hacia el dominio Notification.
 * Implementación actual: LocalNotificationAdapter (llamada directa).
 * En microservicios: KafkaNotificationAdapter (evento async).
 */
public interface NotificationPort {

    void notifyUser(Long userId, String title, String message);
}
