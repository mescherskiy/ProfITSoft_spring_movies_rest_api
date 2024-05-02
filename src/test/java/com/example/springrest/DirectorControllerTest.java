package com.example.springrest;

import com.example.springrest.model.Director;
import com.example.springrest.repository.DirectorRepository;
import com.example.springrest.service.DirectorService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = SpringRestApplication.class
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DirectorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private DirectorRepository directorRepository;
    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    public void beforeEach() {
        directorRepository.deleteAll();
    }

    @Test
    public void testCreateDirector_Success() throws Exception {
        String name = "Steven Spielberg";
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(name);
        mockMvc.perform(post("/api/director")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("Director successfully created"));

        Director director = directorRepository.findAll().get(0);
        assertThat(director).isNotNull();
        assertThat(director.getName()).isEqualTo(name);
    }

    @Test
    public void testCreateDirector_AlreadyExists() throws Exception {
        String name = "Steven Spielberg";
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(name);
        directorRepository.save(new Director(name));
        mockMvc.perform(post("/api/director")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Director with name " + name + " already exists"));

        assertThat(directorRepository.count()).isEqualTo(1L);
    }

    @Test
    public void testCreateDirector_NoNameParam() throws Exception {
        mockMvc.perform(post("/api/director")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Parameter NAME is missing"));

        assertThat(directorRepository.count()).isEqualTo(0);
    }

    @Test
    public void testCreateDirector_EmptyRequestBody() throws Exception {
        mockMvc.perform(post("/api/director")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        assertThat(directorRepository.count()).isEqualTo(0);
    }

    @Test
    public void testGetAllDirectors() throws Exception {
        List<Director> directors = List.of(new Director("Test1"), new Director("Test2"), new Director("Test3"));
        directorRepository.saveAll(directors);

        MvcResult mvcResult = mockMvc.perform(get("/api/director"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString();
        List<Director> directorsFromResponse = objectMapper.readValue(jsonResponse, new TypeReference<List<Director>>() {});

        assertEquals(directors.size(), directorsFromResponse.size());
        assertTrue(directors.containsAll(directorsFromResponse));
        assertTrue(directorsFromResponse.containsAll(directors));
    }

    @Test
    public void testEditDirector_Success() throws Exception {
        String name = "Test1";
        String newName = "Test2";
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(newName);
        directorRepository.save(new Director(name));

        Long id = directorRepository.findAll().get(0).getId();

        mockMvc.perform(put("/api/director/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Edited!"));

        Director director = directorRepository.findById(id).orElse(null);

        assertThat(director).isNotNull();
        assertEquals(newName, director.getName());
    }

    @Test
    public void testEditDirector_NotFound() throws Exception {
        String newName = "Test2";
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(newName);
        long wrongId = -123L;
        mockMvc.perform(put("/api/director/" + wrongId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Director with id " + wrongId + " not found"));
    }

    @Test
    public void testEditDirector_DuplicateName() throws Exception {
        String name = "Test1";
        String newName = "Test2";
        directorRepository.save(new Director(name));
        directorRepository.save(new Director(newName));
        String json = """
                {
                    "name": "%s"
                }
                """.formatted(newName);
        long id = directorRepository.findByName(name).get().getId();

        mockMvc.perform(put("/api/director/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(content().string("Director with name " + newName + " already exists"));
    }

    @Test
    public void testDeleteDirector_Success() throws Exception {
        String name = "Test1";
        Director director = new Director(name);
        directorRepository.save(director);
        long id = directorRepository.findByName(name).orElse(null).getId();

        mockMvc.perform(delete("/api/director/" + id))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted"));
    }

    @Test
    public void testDeleteDirector_NotFound() throws Exception {
        String name = "Test1";
        Director director = new Director(name);
        directorRepository.save(director);
        long id = directorRepository.findByName(name).orElse(null).getId();
        long wrongId = -123;

        mockMvc.perform(delete("/api/director/" + wrongId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Director with this ID not found"));

        assertNotEquals(id, wrongId);
    }


}
