package com.example.springrest.dto;

public record MovieUploadResponseDTO(int imported, int alreadyExists, int failed) {
}
