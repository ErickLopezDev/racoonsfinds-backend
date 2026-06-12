package com.racoonsfinds.backend.service.adapter;

import org.springframework.stereotype.Component;

import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.service.port.UserDirectoryPort;
import com.racoonsfinds.backend.service.port.UserSnapshot;
import com.racoonsfinds.backend.shared.exception.NotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador local: accede al dominio Identity via DB directa.
 * Reemplazar por HttpUserDirectoryAdapter al extraer identity-service.
 */
@Component
@RequiredArgsConstructor
public class LocalUserDirectoryAdapter implements UserDirectoryPort {

    private final UserRepository userRepository;

    @Override
    public UserSnapshot findById(Long userId) {
        return userRepository.findById(userId)
                .map(u -> new UserSnapshot(u.getId(), u.getUsername()))
                .orElseThrow(() -> new NotFoundException("User not found with ID " + userId));
    }
}
