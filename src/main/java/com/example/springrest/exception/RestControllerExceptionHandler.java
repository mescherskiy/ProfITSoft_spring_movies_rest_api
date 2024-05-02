package com.example.springrest.exception;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestControllerExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage() : "";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value()).body("Wrong arguments. " + exceptionMessage);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND.value()).body(e.getMessage() != null ? e.getMessage() : "Entity not found.");
    }

    @ExceptionHandler(EntityExistsException.class)
    protected ResponseEntity<?> handleEntityExistsException(EntityExistsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT.value()).body("Entity already exists.");
    }
}
