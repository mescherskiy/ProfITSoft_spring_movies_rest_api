package com.example.springrest.dto;

import java.util.Set;

public record MovieUpdateDTO(String title, int year, Set<String> genre, String director) {}
