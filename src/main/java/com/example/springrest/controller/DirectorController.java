package com.example.springrest.controller;

import com.example.springrest.dto.RestResponse;
import com.example.springrest.model.Director;
import com.example.springrest.service.DirectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/director")
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<?> getAllDirectors() {
        List<Director> directors = directorService.getAll();
        return ResponseEntity.ok(directors);
    }

    @PostMapping
    public ResponseEntity<?> addNewDirector(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body("Parameter NAME is missing");
        }
        Director dir = directorService.create(name);
        return dir != null ? ResponseEntity.status(HttpStatus.CREATED).body("Director successfully created")
                : ResponseEntity.badRequest().body("Director with name " + name + " already exists");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editDirector(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null) {
            return ResponseEntity.badRequest().body("Parameter NAME is missing");
        }
        RestResponse response = directorService.edit(id, name);
        return ResponseEntity.status(response.status()).body(response.message());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDirector(@PathVariable Long id) {
        boolean deleted = directorService.delete(id);
        return deleted ? ResponseEntity.ok().body("Deleted")
                : ResponseEntity.status(404).body("Director with this ID not found");
    }
}
