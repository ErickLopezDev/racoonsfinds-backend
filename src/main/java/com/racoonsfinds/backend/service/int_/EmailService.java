package com.racoonsfinds.backend.service.int_;
public interface EmailService {
    void sendVerificationEmail(String to, String subject, String body);
}
