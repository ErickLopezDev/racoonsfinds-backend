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
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import jakarta.transaction.Transactional;

import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.dto.user.UserResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final S3Service s3Service;
  public UserDto getCurrentUser() {
    Long currentUserId = AuthUtil.getAuthenticatedUserId();
    if (currentUserId == null) return null;

    User currentUser = userRepository.findById(currentUserId).orElse(null);
    if (currentUser == null) return null;

    return MapperUtil.map(currentUser, UserDto.class);
  }

  @Transactional
  public UserResponseDto updateUser(String username, String imageUrl, LocalDate birthDate, MultipartFile file) throws IOException {
      Long currentUserId = AuthUtil.getAuthenticatedUserId();
      if (currentUserId == null) {
          throw new AccessDeniedException("No authenticated user found");
      }
      User existing = userRepository.findById(currentUserId)
              .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + currentUserId));

      System.out.println("=== ANTES ===");
      System.out.println("Username: " + existing.getUsername());
      System.out.println("ImageUrl: " + existing.getImageUrl());
      System.out.println("BirthDate: " + existing.getBirthDate());

      // Actualizar solo los campos enviados
      if (username != null && !username.isBlank()) {
          existing.setUsername(username);
      }

      if (birthDate != null) {
          existing.setBirthDate(birthDate);
      }

      if (file != null && !file.isEmpty()) {
          String key = s3Service.uploadFile(file, "users");
          existing.setImageUrl(key);
      } else if (imageUrl != null && !imageUrl.isBlank()) {
          existing.setImageUrl(imageUrl);
      }

      User saved = userRepository.save(existing);

      System.out.println("=== DESPUÉS ===");
      System.out.println("Username: " + saved.getUsername());
      System.out.println("ImageUrl: " + saved.getImageUrl());
      System.out.println("BirthDate: " + saved.getBirthDate());

      return mapToDto(saved);
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
