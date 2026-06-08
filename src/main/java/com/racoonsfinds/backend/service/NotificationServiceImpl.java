package com.racoonsfinds.backend.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.model.Notification;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.NotificationRepository;
import com.racoonsfinds.backend.service.int_.NotificationService;
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

        // Proxy por ID para evitar query extra al crear la referencia
        User user = new User();
        user.setId(userId);
        notification.setUser(user);

        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByDateDesc(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notificación no encontrada"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("No puedes modificar notificaciones de otro usuario");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
