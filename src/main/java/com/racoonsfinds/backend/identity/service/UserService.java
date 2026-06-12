package com.racoonsfinds.backend.identity.service;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.identity.dto.user.UserDto;
import com.racoonsfinds.backend.identity.dto.user.UserResponseDto;

public interface UserService {

    UserDto getCurrentUser();

    UserResponseDto updateUser(String username, String imageUrl, LocalDate birthDate, MultipartFile file) throws IOException;
}
