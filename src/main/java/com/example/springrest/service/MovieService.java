package com.example.springrest.service;

import com.example.springrest.dto.*;
import com.example.springrest.model.Director;
import com.example.springrest.model.Movie;
import com.example.springrest.repository.DirectorRepository;
import com.example.springrest.repository.MovieRepository;
import com.example.springrest.service.utils.GenreAdapter;
import com.example.springrest.service.utils.MovieSpecifications;
import com.example.springrest.service.utils.YearAdapter;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.opencsv.CSVWriter;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;

    public RestResponse addMovie(MovieCreateDTO movieCreateDTO) {
        if (movieRepository.existsByTitleAndDirector_Name(movieCreateDTO.title(), movieCreateDTO.director())) {
            throw new EntityExistsException();
        }
        Movie movie = fromDTO(movieCreateDTO);
        Long id = movieRepository.save(movie).getId();
        return new RestResponse(201, String.valueOf(id));
    }

    public MovieDetailedDTO getMovieById(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return new MovieDetailedDTO(movie.getId(), movie.getTitle(), movie.getYear(), movie.getGenre(), movie.getDirector());
    }

    public void updateMovie(Long id, MovieUpdateDTO movieUpdateDTO) {
        Movie movie = movieRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        if (movieUpdateDTO.title() == null && movieUpdateDTO.year() == 0
                && movieUpdateDTO.genre() == null && movieUpdateDTO.director() == null) {
            throw new IllegalArgumentException("Nothing to update");
        }
        updateMovieFields(movie, movieUpdateDTO);
        movieRepository.save(movie);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        movieRepository.delete(movie);
    }

    public Page<MovieInfoDTO> findMoviesByQuery(MovieQueryListDTO dto) {
        PageRequest pageRequest = PageRequest.of(dto.page() - 1, dto.size());
        MovieQueryDTO movieQueryDTO = new MovieQueryDTO(dto.title(), dto.year(), dto.genre(), dto.director());
        Specification<Movie> specification = getMovieSpecification(movieQueryDTO);
        Page<Movie> moviesPage = movieRepository.findAll(specification, pageRequest);
        return moviesPage.map(movie -> new MovieInfoDTO(
                movie.getTitle(),
                movie.getYear(),
                movie.getGenre().toString(),
                movie.getDirector().getName()
        ));
    }

    public void generateReport(MovieQueryDTO dto, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movies.csv");
        Specification<Movie> specification = getMovieSpecification(dto);
        List<Movie> movies = movieRepository.findAll(specification);
        if (movies.isEmpty()) {
            throw new EntityNotFoundException("Movies not found");
        }
        String csv = convertMoviesToCSV(movies);
        csv = csv.replace("\"", "");
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);

        try {
            response.getOutputStream().write(csvBytes);
            response.getOutputStream().flush();
        } catch (IOException e) {
            System.out.println("Error generating csv-file. " + e.getMessage());
        }
    }

    @Transactional
    public MovieUploadResponseDTO uploadMovies(MultipartFile file) {
        try {
            int imported = 0;
            int alreadyExists = 0;
            int failed = 0;

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Integer.class, new YearAdapter())
                    .registerTypeAdapter(Set.class, new GenreAdapter())
                    .create();

            try (BufferedReader bf = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
                JsonReader reader = new JsonReader(bf);
                reader.beginArray();
                while (reader.hasNext()) {
                    MovieCreateDTO dto = gson.fromJson(reader, MovieCreateDTO.class);
                    if (!isValidMovie(dto)) {
                        failed++;
                        continue;
                    }
                    if (saveMovie(dto)) {
                        imported++;
                    } else {
                        alreadyExists++;
                    }
                }
            }
            return new MovieUploadResponseDTO(imported, alreadyExists, failed);
        } catch (JsonIOException | JsonSyntaxException |MalformedJsonException e) {
            throw new IllegalArgumentException("Invalid JSON file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isValidMovie(MovieCreateDTO dto) {
        return dto.title() != null && !dto.title().isBlank()
                && dto.year() != null && dto.year() >= 1900 && dto.year() <= Year.now().getValue()
                && dto.genre() != null && !dto.genre().isEmpty()
                && dto.director() != null && !dto.director().isBlank();
    }

    private boolean saveMovie(MovieCreateDTO dto) {
        Movie movie = new Movie(
                dto.title(),
                dto.year(),
                dto.genre(),
                getOrCreateDirector(dto.director())
        );

        if (!movieRepository.existsByTitleAndDirector_Name(movie.getTitle(), movie.getDirector().getName())) {
            movieRepository.save(movie);
            return true;
        }
        return false;
    }

    private Movie fromDTO(MovieCreateDTO movieCreateDTO) {
        Director director = directorRepository.findByName(movieCreateDTO.director()).orElseGet(() -> {
            Director newDirector = new Director(movieCreateDTO.director());
            return directorRepository.save(newDirector);
        });

        return new Movie(movieCreateDTO.title(), movieCreateDTO.year(), movieCreateDTO.genre(), director);
    }

    private Specification<Movie> getMovieSpecification(MovieQueryDTO dto) {
        List<Specification<Movie>> specifications = new ArrayList<>();

        if (dto.title() != null && !dto.title().isBlank()) {
            specifications.add(MovieSpecifications.hasTitle(dto.title()));
        }

        if (dto.year() != 0) {
            specifications.add(MovieSpecifications.hasYear(dto.year()));
        }

        if (dto.genre() != null && !dto.genre().isEmpty()) {
            for (String genre : dto.genre()) {
                specifications.add(MovieSpecifications.hasGenre(genre));
            }
        }

        if (dto.director() != null && !dto.director().isBlank()) {
            specifications.add(MovieSpecifications.hasDirectorName(dto.director()));
        }

        Specification<Movie> finalSpecification = specifications.stream()
                .reduce(Specification::and).orElseThrow(IllegalArgumentException::new);
        return finalSpecification;
    }

    private void updateMovieFields(Movie movie, MovieUpdateDTO movieUpdateDTO) {
        if (movieUpdateDTO.title() != null && !movieUpdateDTO.title().isBlank()) {
            movie.setTitle(movieUpdateDTO.title());
        }
        if (movieUpdateDTO.year() != 0 && movieUpdateDTO.year() < Year.now().getValue()) {
            movie.setYear(movieUpdateDTO.year());
        }
        if (movieUpdateDTO.genre() != null && !movieUpdateDTO.genre().isEmpty()) {
            movie.setGenre(movieUpdateDTO.genre());
        }
        if (movieUpdateDTO.director() != null && !movieUpdateDTO.director().isBlank()) {
            Director director = directorRepository.findByName(movieUpdateDTO.director()).orElseGet(() -> {
                Director newDirector = new Director(movieUpdateDTO.director());
                directorRepository.save(newDirector);
                return newDirector;
            });
            movie.setDirector(director);
        }
    }

    private String convertMoviesToCSV(List<Movie> movies) {
        StringWriter sw = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(sw)) {
            csvWriter.writeNext(new String[]{"Id", "Title", "Year", "Genre", "Director"});

            for (Movie movie : movies) {
                csvWriter.writeNext(new String[]{
                        String.valueOf(movie.getId()),
                        movie.getTitle(),
                        String.valueOf(movie.getYear()),
                        String.join(",", movie.getGenre()),
                        movie.getDirector().getName()
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sw.toString();
    }


    private Director getOrCreateDirector(String directorName) {
        Director director = directorRepository.findByName(directorName).orElse(new Director(directorName));
        if (director.getId() == null) {
            directorRepository.save(director);
        }
        return director;
    }

}
