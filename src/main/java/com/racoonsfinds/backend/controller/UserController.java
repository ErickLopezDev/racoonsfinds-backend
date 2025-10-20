package com.racoonsfinds.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.racoonsfinds.backend.dto.ApiResponse;
import com.racoonsfinds.backend.dto.user.UserDto;
import com.racoonsfinds.backend.service.UserService;
import com.racoonsfinds.backend.shared.utils.ResponseUtil;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
  
  private final UserService userService;

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserDto>> getCurrentUser() {
    return ResponseUtil.ok("Usuario obtenido correctamente", userService.getCurrentUser()); 
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserDto dto) {
    return ResponseUtil.ok("Usuario actualizado correctamente", userService.updateUser(id, dto));
  }
}
