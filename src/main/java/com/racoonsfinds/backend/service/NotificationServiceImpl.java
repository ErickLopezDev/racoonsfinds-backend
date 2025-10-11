package com.racoonsfinds.backend.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.model.Notification;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.NotificationRepository;
import com.racoonsfinds.backend.service.int_.NotificationService;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(Long userId, String title, String message) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setDate(LocalDate.now());
        notification.setRead(false);

        // Solo asociamos el user por id para evitar query extra
        User user = new User();
        user.setId(userId);
        notification.setUser(user);

        return notificationRepository.save(notification);
    }
}