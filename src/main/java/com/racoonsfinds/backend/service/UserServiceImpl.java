package com.racoonsfinds.backend.service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.dto.user.UserResponseDto;
import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.service.int_.UserService;
import com.racoonsfinds.backend.shared.exception.NotFoundException;
import com.racoonsfinds.backend.shared.exception.UnauthorizedException;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public UserDto getCurrentUser() {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        if (currentUserId == null) throw new UnauthorizedException("Usuario no autenticado");

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        return MapperUtil.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(String username, String imageUrl, LocalDate birthDate, MultipartFile file) throws IOException {
        Long currentUserId = AuthUtil.getAuthenticatedUserId();
        if (currentUserId == null) throw new UnauthorizedException("Usuario no autenticado");

        User existing = userRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID " + currentUserId));

        if (username != null && !username.isBlank()) {
            existing.setUsername(username);
        }

        if (birthDate != null) {
            existing.setBirthDate(birthDate);
        }

        if (file != null && !file.isEmpty()) {
            String key = s3Service.uploadFile(file, "users");
            existing.setImageUrl(s3Service.getFileUrl(key));
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            existing.setImageUrl(imageUrl);
        }

        return mapToDto(userRepository.save(existing));
    }

    private UserResponseDto mapToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setBirthDate(user.getBirthDate());
        dto.setImageUrl(user.getImageUrl());
        return dto;
    }
}
