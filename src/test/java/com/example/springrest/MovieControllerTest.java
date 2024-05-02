package com.example.springrest;

import com.example.springrest.dto.MovieCreateDTO;
import com.example.springrest.dto.MovieDetailedDTO;
import com.example.springrest.dto.MovieInfoDTO;
import com.example.springrest.dto.MovieResponseListDTO;
import com.example.springrest.model.Director;
import com.example.springrest.model.Movie;
import com.example.springrest.repository.DirectorRepository;
import com.example.springrest.repository.MovieRepository;
import com.example.springrest.service.MovieService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SpringRestApplication.class
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MovieControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void beforeEach() {
        movieRepository.deleteAll();
    }

    @Test
    protected void testAddMovie_Success() throws Exception {
        String title = "Test Title";
        String director = "Test Director";
        String json = """
                {
                    "title": "%s",
                    "year": 2000,
                    "genre": ["Comedy", "Fantasy"],
                    "director": "%s"
                }
                """.formatted(title, director);
        long moviesCount = movieRepository.count();

        MvcResult mvcResult = mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readValue(jsonResponse, new TypeReference<Long>() {
        });

        Movie movie = movieRepository.findByTitleAndDirector_Name(title, director).orElse(null);
        assertThat(movie).isNotNull();
        assertEquals(id, movie.getId());
        assertEquals(title, movie.getTitle());
        assertEquals(director, movie.getDirector().getName());
        assertEquals(moviesCount + 1, movieRepository.count());
    }

    @Test
    protected void testAddMovie_InvalidAttributes() throws Exception {
        String name = "Test Title";
        String director = "Test Director";
        String json = """
                {
                    "name": "%s",
                    "year": "2000",
                    "genre": "Comedy", "Fantasy",
                    "director": "%s"
                }
                """.formatted(name, director);
        long moviesCount = movieRepository.count();

        mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(moviesCount, movieRepository.count());
    }

    @Test
    protected void testAddMovie_AlreadyExists() throws Exception {
        String title = "Test Title";
        int year = 2000;
        Set<String> genre = Set.of("Genre1", "Genre2");
        String director = "Test Director";
        String json = """
                {
                    "title": "%s",
                    "year": "%d",
                    "genre": ["Genre1", "Genre2"],
                    "director": "%s"
                }
                """.formatted(title, year, director);
        movieService.addMovie(new MovieCreateDTO(title, year, genre, director));
        long moviesCount = movieRepository.count();

        mockMvc.perform(post("/api/movie")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        assertEquals(moviesCount, movieRepository.count());
    }

    @Test
    protected void testGetMovieById_Success() throws Exception {
        Movie movie = createTestMovie();

        long id = movieRepository.save(movie).getId();

        MvcResult mvcResult = mockMvc.perform(get("/api/movie/" + id))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        MovieDetailedDTO dto = objectMapper.readValue(jsonResponse, new TypeReference<MovieDetailedDTO>() {
        });

        assertEquals(movie.getTitle(), dto.title());
        assertEquals(movie.getYear(), dto.year());
        assertEquals(movie.getGenre(), dto.genre());
        assertEquals(movie.getDirector(), dto.director());
        assertEquals(id, dto.id());
    }

    @Test
    protected void testGetMovieById_NotFound() throws Exception {
        mockMvc.perform(get("/api/movie/-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    protected void testUpdateMovie_Success() throws Exception {
        String newTitle = "New title";
        String json = """
                {
                    "title": "%s"
                }
                """.formatted(newTitle);
        Movie movie = createTestMovie();
        long id = movieRepository.save(movie).getId();

        mockMvc.perform(put("/api/movie/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Updated!"));

        Movie updatedMovie = movieRepository.findById(id).orElse(null);

        assertThat(updatedMovie).isNotNull();
        assertEquals(newTitle, updatedMovie.getTitle());
        assertNotEquals(movie.getTitle(), updatedMovie.getTitle());
        assertEquals(movie.getId(), updatedMovie.getId());
        assertEquals(movie.getYear(), updatedMovie.getYear());
        assertEquals(movie.getDirector(), updatedMovie.getDirector());
    }

    @Test
    protected void testUpdateMovie_InvalidParams() throws Exception {
        String newTitle = "New title";
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(newTitle);
        Movie movie = createTestMovie();
        long id = movieRepository.save(movie).getId();

        mockMvc.perform(put("/api/movie/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    protected void testUpdateMovie_WrongId() throws Exception {
        String newTitle = "New title";
        String json = """
                {
                    "title": "%s"
                }
                """.formatted(newTitle);
        mockMvc.perform(put("/api/movie/-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    protected void testDeleteMovie_Success() throws Exception {
        Movie movie = createTestMovie();
        long id = movieRepository.save(movie).getId();

        mockMvc.perform(delete("/api/movie/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted!"));

        Movie deletedMovie = movieRepository.findById(id).orElse(null);

        assertThat(deletedMovie).isNull();
    }

    @Test
    protected void testDeleteMovie_WrongId() throws Exception {
        mockMvc.perform(delete("/api/movie/-123"))
                .andExpect(status().isNotFound());
    }

    @Test
    protected void testFindMoviesByQuery_Success() throws Exception {
        Director director1 = new Director("Steven Spielberg");
        Director director2 = new Director("James Cameron");
        Director director3 = new Director("Quentin Tarantino");
        directorRepository.saveAll(List.of(director1, director2, director3));
        int year = 2000;
        Movie movie1 = new Movie("Title1", 1990, Set.of("Genre1"), director1);
        Movie movie2 = new Movie("Title2", year, Set.of("Genre1", "Genre2"), director2);
        Movie movie3 = new Movie("Title3", year, Set.of("Genre1", "Genre2", "Genre3"), director3);
        Movie movie4 = new Movie("Title4", year, Set.of("Genre2", "Genre3"), director2);
        Movie movie5 = new Movie("Title5", year, Set.of("Genre3"), director2);
        movieRepository.saveAll(List.of(movie1, movie2, movie3, movie4, movie5));
        String json = """
                {
                    "year": %d,
                    "genre": ["Genre3"],
                    "director": "%s",
                    "page": 1,
                    "size": 1
                }
                """.formatted(year, director2.getName());

        MvcResult mvcResult = mockMvc.perform(post("/api/movie/_list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        MovieResponseListDTO response = objectMapper.readValue(jsonResponse, new TypeReference<MovieResponseListDTO>() {
        });

        List<MovieInfoDTO> movies = response.list();

        assertEquals(movies.size(), 1);
        assertEquals(response.totalPages(), 2);
        assertEquals(movies.get(0).year(), year);
        assertTrue(movies.get(0).genre().contains("Genre3"));
        assertEquals(movies.get(0).directorName(), director2.getName());
        assertEquals(movies.get(0).title(), "Title4");
    }

    @Test
    protected void testFindMoviesByQuery_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/movie/_list"))
                .andExpect(status().isBadRequest());
    }

    @Test
    protected void testGenerateReport_Success() throws Exception {
        Movie movie = createTestMovie();
        movieRepository.save(movie);
        String json = """
                {
                    "year": %d,
                    "director": "%s"
                }
                """.formatted(movie.getYear(), movie.getDirector().getName());

        mockMvc.perform(post("/api/movie/_report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition", equalTo("attachment; filename=movies.csv")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM));
    }

    @Test
    protected void testGenerateReport_EmptyBody() throws Exception {
        mockMvc.perform(post("/api/movie/_report"))
                .andExpect(status().isBadRequest());
    }

    @Test
    protected void testUploadMovies_Success() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/movies.json");
        MockMultipartFile file = new MockMultipartFile("file", "movies.json",
                MediaType.APPLICATION_JSON_VALUE, inputStream);
        mockMvc.perform(multipart("/api/movie/upload")
                .file(file))
                .andExpect(status().isCreated());
    }

    @Test
    protected void testUploadMovies_NoFile() throws Exception {
        mockMvc.perform(multipart("/api/movie/upload"))
                .andExpect(status().isBadRequest());
    }

    @Test
    protected void testUploadMovies_WrongFileFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt",
                MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        mockMvc.perform(multipart("/api/movie/upload")
                .file(file))
                .andExpect(status().isBadRequest());
    }


    private Movie createTestMovie() {
        String title = "Test Title";
        int year = 2000;
        Set<String> genre = Set.of("Genre1", "Genre2");
        Director director = new Director("Test director");
        directorRepository.save(director);
        return new Movie(title, year, genre, director);
    }

}
