package com.racoonsfinds.backend.dto.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDto {

    private Long id;
    private Integer stars;
    private String comment;
    private LocalDate date;
    private Long userId;
    private String userName;
    private Long productId;
}