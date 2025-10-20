package com.racoonsfinds.backend.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.racoonsfinds.backend.model.User;
import com.racoonsfinds.backend.repository.UserRepository;
import com.racoonsfinds.backend.shared.utils.AuthUtil;
import com.racoonsfinds.backend.shared.utils.MapperUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import com.racoonsfinds.backend.dto.user.UserDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  public UserDto getCurrentUser() {
    Long currentUserId = AuthUtil.getAuthenticatedUserId();
    if (currentUserId == null) return null;

    User currentUser = userRepository.findById(currentUserId).orElse(null);
    if (currentUser == null) return null;

    return MapperUtil.map(currentUser, UserDto.class);
  }

  //TODO: Arreglar persistencia y mapeo
  @Transactional
  public UserDto updateUser(Long id, UserDto dto) {
    Long currentUserId = AuthUtil.getAuthenticatedUserId();

    System.out.println("UPDATE ===== ID ACTUAL: " + currentUserId + " / ID A EDITAR: " + id);
    if (!id.equals(currentUserId)) {
      throw new AccessDeniedException("No puedes editar otra cuenta que no sea la tuya");
    }

    User existingUser = userRepository.findById(id)
      .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));

    System.out.println("USUARIO ANTES DE MAPEAR: " + existingUser);
    MapperUtil.map(dto, existingUser);

    userRepository.save(existingUser);
    System.out.println("USUARIO DESPUES DE GUARDAR: " + existingUser);
    return MapperUtil.map(existingUser, UserDto.class);
  }
}
