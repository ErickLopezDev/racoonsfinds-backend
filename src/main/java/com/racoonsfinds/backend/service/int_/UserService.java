package com.racoonsfinds.backend.service.int_;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.dto.user.UserResponseDto;

public interface UserService {

    UserDto getCurrentUser();

    UserResponseDto updateUser(String username, String imageUrl, LocalDate birthDate, MultipartFile file) throws IOException;
}
