package com.example.springrest.dto;

import com.example.springrest.model.Movie;

import java.util.List;

public record MovieResponseListDTO(List<MovieInfoDTO> list, int totalPages) {
}
