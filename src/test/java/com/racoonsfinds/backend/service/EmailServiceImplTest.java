package com.racoonsfinds.backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import com.racoonsfinds.backend.service.int_.EmailService;
import com.resend.Resend;
import com.resend.services.emails.Emails;
import com.resend.services.emails.model.CreateEmailOptions;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private Resend resend;

    @Mock
    private Emails emails;

    @Mock
    private Logger log;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simulamos que resend.emails() devuelve el mock "emails"
        when(resend.emails()).thenReturn(emails);

        // Creamos la instancia de EmailServiceImpl con valores ficticios
        emailService = new EmailServiceImpl("fake-api-key", "from@test.com");

        // Inyectamos manualmente el mock Resend dentro del servicio usando reflexión
        try {
            var field = EmailServiceImpl.class.getDeclaredField("resend");
            field.setAccessible(true);
            field.set(emailService, resend);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    void sendVerificationEmail_ShouldSendSuccessfully() throws Exception {
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(null);

        emailService.sendVerificationEmail("user@test.com", "Verificación", "<b>Bienvenido!</b>");

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }


    @Test
    void sendPasswordResetEmail_ShouldSendSuccessfully() throws Exception {
        when(emails.send(any(CreateEmailOptions.class))).thenReturn(null); 
        
        emailService.sendPasswordResetEmail("user@test.com", "Reset", "<b>Nuevo password</b>");

        verify(emails, times(1)).send(any(CreateEmailOptions.class));
    }

    @Test
    void sendEmail_ShouldLogError_WhenExceptionOccurs() throws Exception {
        doThrow(new RuntimeException("Error simulado"))
            .when(emails)
            .send(any(CreateEmailOptions.class));

        emailService.sendVerificationEmail("user@test.com", "Fallo", "<b>Error</b>");
    }

}