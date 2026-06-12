package com.racoonsfinds.backend.notification.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.notification.domain.Notification;
import com.racoonsfinds.backend.notification.repository.NotificationRepository;
import com.racoonsfinds.backend.notification.service.NotificationService;
import com.racoonsfinds.backend.shared.exception.ForbiddenException;
import com.racoonsfinds.backend.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(Long userId, String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDate(LocalDate.now());
        notification.setRead(false);
        notification.setUserId(userId);

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByDateDesc(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));

        if (!notification.getUserId().equals(userId)) {
            throw new ForbiddenException("No puedes modificar notificaciones de otro usuario");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
