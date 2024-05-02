package com.example.springrest.dto;

import jakarta.validation.constraints.Min;

import java.util.Set;

public record MovieQueryListDTO(String title,
                                int year,
                                Set<String> genre,
                                String director,
                                int page,
                                int size) {}
