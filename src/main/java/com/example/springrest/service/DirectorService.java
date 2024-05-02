package com.example.springrest.service;

import com.example.springrest.dto.RestResponse;
import com.example.springrest.model.Director;
import com.example.springrest.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public List<Director> getAll() {
        return directorRepository.findAll();
    }

    public Director create(String director) {
        if (directorRepository.existsByName(director)) {
            return null;
        }
        Director newDirector = new Director(director);
        return directorRepository.save(newDirector);
    }

    @Transactional
    public RestResponse edit(Long id, String name) {
        Director director = directorRepository.findById(id).orElse(null);
        if (director == null) {
            return new RestResponse(404, "Director with id " + id + " not found");
        } else if (directorRepository.existsByName(name)){
            return new RestResponse(409, "Director with name " + name + " already exists");
        } else {
            director.setName(name);
            directorRepository.save(director);
            return new RestResponse(200, "Edited!");
        }
    }

    public boolean delete(Long id) {
        if (directorRepository.existsById(id)) {
            directorRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
