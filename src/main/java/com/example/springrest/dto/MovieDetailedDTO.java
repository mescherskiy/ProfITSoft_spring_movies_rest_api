package com.example.springrest.dto;

import com.example.springrest.model.Director;

import java.util.Set;

public record MovieDetailedDTO(Long id, String title, int year, Set<String> genre, Director director) {
}
