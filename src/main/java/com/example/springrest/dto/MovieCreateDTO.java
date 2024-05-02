package com.example.springrest.dto;

import jakarta.validation.constraints.*;

import java.util.Set;

public record MovieCreateDTO(
        @NotBlank(message = "Title is required")
        String title,
        @Min(value = 1900, message = "Year can't be less than 1900")
        @Max(value = 2100, message = "Year is too big")
        Integer year,
        @NotNull(message = "Genre is required")
        @Size(min = 1, message = "At least one genre must be specified")
        Set<String> genre,
        @NotBlank(message = "Director is required")
        String director
) {
}
