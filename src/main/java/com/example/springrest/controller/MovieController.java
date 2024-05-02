package com.example.springrest.controller;

import com.example.springrest.dto.*;
import com.example.springrest.service.MovieService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<?> addMovie(@Valid @RequestBody MovieCreateDTO movieCreateDTO) {
        RestResponse response = movieService.addMovie(movieCreateDTO);
        return ResponseEntity.status(response.status()).body(response.message());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable Long id) {
        MovieDetailedDTO movie = movieService.getMovieById(id);
        return ResponseEntity.ok(movie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(@PathVariable Long id, @RequestBody MovieUpdateDTO movieUpdateDTO) {
        movieService.updateMovie(id, movieUpdateDTO);
        return ResponseEntity.ok("Updated!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok("Deleted!");
    }

    @PostMapping("/_list")
    public ResponseEntity<?> findMoviesByQuery(@RequestBody MovieQueryListDTO dto) {
        Page<MovieInfoDTO> pages = movieService.findMoviesByQuery(dto);
        return ResponseEntity.ok(new MovieResponseListDTO(pages.getContent(), pages.getTotalPages()));
    }

    @PostMapping(value = "/_report", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void generateReport(@RequestBody MovieQueryDTO dto, HttpServletResponse response) {
        movieService.generateReport(dto, response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMovies(@RequestParam("file") MultipartFile file) {
        MovieUploadResponseDTO result = movieService.uploadMovies(file);
        return result.imported() > 0
                ? ResponseEntity.status(HttpStatus.CREATED).body(result)
                : ResponseEntity.ok().body(result);
    }
}
