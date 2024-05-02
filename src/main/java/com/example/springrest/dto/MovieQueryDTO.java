package com.example.springrest.dto;

import java.util.Set;

public record MovieQueryDTO(String title, int year, Set<String> genre, String director) {
}
