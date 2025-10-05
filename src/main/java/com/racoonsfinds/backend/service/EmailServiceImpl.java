package com.racoonsfinds.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.racoonsfinds.backend.service.int_.EmailService;
import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final Resend resend;
    private final String fromEmail;

    public EmailServiceImpl(@Value("${resend.api-key}") String apiKey,
                            @Value("${resend.from}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendVerificationEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(request);
            log.info("Correo enviado correctamente a {}", to);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage());
        }
    }
}
