package com.racoonsfinds.backend.service;

import com.racoonsfinds.backend.model.Notification;
import com.racoonsfinds.backend.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_ShouldPersistWithDefaults() {
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification n = notificationService.createNotification(1L, "Titulo", "Mensaje");

        assertNotNull(n);
        assertEquals("Titulo", n.getTitle());
        assertEquals("Mensaje", n.getMessage());
        assertFalse(Boolean.TRUE.equals(n.getRead()));
        assertEquals(LocalDate.now(), n.getDate());
        assertNotNull(n.getUser());
        assertEquals(1L, n.getUser().getId());

        verify(notificationRepository).save(any(Notification.class));
    }
}

