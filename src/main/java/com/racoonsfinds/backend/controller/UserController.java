package com.racoonsfinds.backend.controller;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.user.UserResponseDto;
import com.racoonsfinds.backend.service.UserService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  
  private final UserService userService;

  @GetMapping("/id")
  public ResponseEntity<ApiResponse<UserResponseDto>> getMe(){
    return ResponseUtil.ok("Información personal del usuario obtenida correctamente", userService.getMe());
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@PathVariable Long id){
    return ResponseUtil.ok("Información de usuario obtenido correctamente", userService.getUserInfo(id));
  }

  @PutMapping(value = "", consumes = {"multipart/form-data"})
  public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
          @RequestParam(required = false) String username,
          @RequestParam(required = false) String imageUrl,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
          @RequestPart(required = false) MultipartFile file
  ) throws IOException {
      UserResponseDto updated = userService.updateUser(username, imageUrl, birthDate, file);
      return ResponseEntity.ok(new ApiResponse<>(
              "Usuario actualizado correctamente", true, updated
      ));
  }

}
