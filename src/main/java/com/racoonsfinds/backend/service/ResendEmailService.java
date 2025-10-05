package com.racoonsfinds.backend.service;

import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.service.int_.EmailService;

@Service
public class ResendEmailService implements EmailService {

    // Inyecta keys/config del proveedor via application.properties
    @Override
    public void sendVerificationEmail(String to, String subject, String body) {
        // TODO: integra con Resend SDK/HTTP. Aquí un stub:
        System.out.printf("Sending verification email to %s: %s%n%s%n", to, subject, body);
        // En producción reemplaza con el cliente real.
    }
}