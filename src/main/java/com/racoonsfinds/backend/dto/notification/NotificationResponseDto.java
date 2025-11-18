package com.racoonsfinds.backend.dto.notification;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private String title;
    private String message;
    private LocalDate date;
    private Boolean read;
}
