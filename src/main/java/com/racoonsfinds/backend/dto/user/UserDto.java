package com.racoonsfinds.backend.dto.user;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserDto {
  private String username;
  private String email;
  private LocalDate birthDate;
  private String imageUrl;
}
