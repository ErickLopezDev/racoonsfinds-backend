package com.racoonsfinds.backend.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 45, message = "El título no puede tener más de 45 caracteres")
    private String title;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 250, message = "El mensaje no puede tener más de 250 caracteres")
    private String message;
}
