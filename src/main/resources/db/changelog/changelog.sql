-- liquibase formatted sql

-- changeset sam:1
CREATE TABLE IF NOT EXISTS Director
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO Director (name)
VALUES ('Steven Spielberg'),
       ('Quentin Tarantino'),
       ('Martin Scorsese'),
       ('Stanley Kubrick'),
       ('James Cameron');


-- changeset sam:2
CREATE TABLE IF NOT EXISTS Movie
(
    id          SERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    pub_year    INT          NOT NULL,
    director_id BIGINT,
    FOREIGN KEY (director_id) REFERENCES Director (id)
);

-- changeset sam:3
CREATE TABLE IF NOT EXISTS Movie_Genre
(
    movie_id BIGINT,
    genre    VARCHAR(255),
    FOREIGN KEY (movie_id) REFERENCES Movie (id)
);

-- changeset sam:4
CREATE INDEX IF NOT EXISTS idx_movie_title ON Movie (title);

-- changeset sam:5
CREATE INDEX IF NOT EXISTS idx_director_name ON Director (name);