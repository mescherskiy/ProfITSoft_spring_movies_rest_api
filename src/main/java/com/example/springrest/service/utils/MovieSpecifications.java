package com.example.springrest.service.utils;

import com.example.springrest.model.Movie;
import org.springframework.data.jpa.domain.Specification;

public class MovieSpecifications {
    public static Specification<Movie> hasYear(int year) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("year"), year);
    }

    public static Specification<Movie> hasGenre(String genre) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isMember(genre, root.get("genre"));
    }

    public static Specification<Movie> hasTitle(String title) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("title"), title);
    }

    public static Specification<Movie> hasDirectorName(String directorName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("director").get("name"), "%" + directorName + "%");
    }
}
