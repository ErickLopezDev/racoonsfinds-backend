package com.racoonsfinds.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.racoonsfinds.backend.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdOrderByDateDesc(Long userId);

}