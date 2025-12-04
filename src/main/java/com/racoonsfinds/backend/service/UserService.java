package com.racoonsfinds.backend.service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.shared.exception.ResourceNotFoundException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;

import jakarta.transaction.Transactional;

import com.racoonsfinds.backend.dto.user.UserResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final S3Service s3Service;

    public UserResponseDto getMe() {
        Long sessionUserId = AuthUtil.getAuthenticatedUserId();
        if (sessionUserId == null) {
            throw new AccessDeniedException("No authenticated user"); 
        }
        User user = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID " + sessionUserId));
        return mapToDto(user);
    }

    public UserResponseDto getUserInfo(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID " + id
                ));
        return mapToDto(user);
    }


    @Transactional
    public UserResponseDto updateUser(String username, String imageUrl, LocalDate birthDate, MultipartFile file) throws IOException {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        if (currentUserId == null) {
            throw new AccessDeniedException("No authenticated user found");
        }
        User existing = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + currentUserId));

        // Actualizar solo los campos enviados
        if (username != null && !username.isBlank()) {
            existing.setUsername(username);
        }

        if (birthDate != null) {
            existing.setBirthDate(birthDate);
        }

        if (file != null && !file.isEmpty()) {
                String key = s3Service.uploadFile(file, "users");
                String fullUrl = s3Service.getFileUrl(key); 
                existing.setImageUrl(fullUrl);
            } else if (imageUrl != null && !imageUrl.isBlank()) {
                existing.setImageUrl(imageUrl);
            }

        User saved = userRepository.save(existing);

        return mapToDto(saved);
    }

    @Transactional
    public void cancelUserAccount() {

        Long sessionUserId = AuthUtil.getAuthenticatedUserId();
        User sessionUser = userRepository.findById(sessionUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + sessionUserId));

        if (sessionUser.getIsAccountCanceled()) {
            throw new RuntimeException("User account has already been canceled");
        }

        sessionUser.setIsAccountCanceled(true);
        userRepository.save(sessionUser);
    }


    private UserResponseDto mapToDto(User user) {
        if (user == null) return null;

        UserResponseDto dto = new UserResponseDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBirthDate(user.getBirthDate());
        dto.setImageUrl(user.getImageUrl());
        return dto;
    }

}
