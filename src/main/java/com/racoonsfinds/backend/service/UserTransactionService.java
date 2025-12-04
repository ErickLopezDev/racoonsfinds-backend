package com.racoonsfinds.backend.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserTransactionService {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementFailedAttempts(User user) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}