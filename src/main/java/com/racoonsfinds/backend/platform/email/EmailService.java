package com.racoonsfinds.backend.platform.email;
public interface EmailService {
    void sendVerificationEmail(String to, String subject, String body);
    void sendPasswordResetEmail(String to, String subject, String body);
}
