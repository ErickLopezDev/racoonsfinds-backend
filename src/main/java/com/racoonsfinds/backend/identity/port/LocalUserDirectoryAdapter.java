package com.racoonsfinds.backend.identity.port;

import org.springframework.stereotype.Component;

import com.racoonsfinds.backend.identity.repository.UserRepository;
import com.racoonsfinds.backend.identity.port.UserDirectoryPort;
import com.racoonsfinds.backend.identity.port.UserSnapshot;
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
