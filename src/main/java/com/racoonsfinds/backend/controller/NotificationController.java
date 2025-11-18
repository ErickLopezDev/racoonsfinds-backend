package com.racoonsfinds.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.notification.NotificationResponseDto;
import com.racoonsfinds.backend.model.Notification;
import com.racoonsfinds.backend.service.int_.NotificationService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Obtener las notificaciones del usuario autenticado.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getMyNotifications() {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        List<NotificationResponseDto> response = notifications.stream()
                .map(n -> new NotificationResponseDto(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getDate(),
                        n.getRead()
                ))
                .toList();

        return ResponseUtil.ok("Listado de notificaciones", response);
    }

    /**
     * Marcar una notificación como leída.
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = AuthUtil.getAuthenticatedUserId();
        if (userId == null) throw new NotFoundException("Usuario no autenticado");

        notificationService.markAsRead(id, userId);
        return ResponseUtil.ok("Notificación marcada como leída");
    }
}
