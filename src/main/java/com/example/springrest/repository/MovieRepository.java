package com.example.springrest.repository;

import com.example.springrest.model.Director;
import com.example.springrest.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findByTitleAndDirector_Name(String title, String directorName);
    boolean existsByTitleAndDirector_Name(String title, String directorName);
}
