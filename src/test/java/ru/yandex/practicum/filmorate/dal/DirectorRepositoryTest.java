package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorRepository.class, DirectorRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class DirectorRepositoryTest {
    final JdbcTemplate jdbc;
    final DirectorRepository directorRepository;
    Director director;

    @BeforeEach
    void setUp() {
        director = new Director();
        director.setFirstName("Christopher");
        director.setLastName("Nolan");
    }

    @AfterEach
    void afterEach() {
        jdbc.update("DELETE FROM film_directors");
        jdbc.update("DELETE FROM director");
    }

    @Test
    void createDirector() {
        Director createdDirector = directorRepository.create(director);

        assertThat(createdDirector)
                .hasFieldOrPropertyWithValue("id", createdDirector.getId())
                .hasFieldOrPropertyWithValue("firstName", "Christopher")
                .hasFieldOrPropertyWithValue("lastName", "Nolan");
    }

    @Test
    void updateDirector() {
        Director createdDirector = directorRepository.create(director);
        createdDirector.setFirstName("Updated");
        createdDirector.setLastName("Director");

        Director updatedDirector = directorRepository.update(createdDirector);

        assertThat(updatedDirector)
                .hasFieldOrPropertyWithValue("id", createdDirector.getId())
                .hasFieldOrPropertyWithValue("firstName", "Updated")
                .hasFieldOrPropertyWithValue("lastName", "Director");
    }

    @Test
    void deleteDirector() {
        Director createdDirector = directorRepository.create(director);
        boolean isDeleted = directorRepository.delete(createdDirector);

        assertThat(isDeleted).isTrue();
        assertThat(directorRepository.findDirectorById(createdDirector.getId())).isEmpty();
    }

    @Test
    void findDirectorById() {
        Director createdDirector = directorRepository.create(director);
        Optional<Director> foundDirector = directorRepository.findDirectorById(createdDirector.getId());

        assertThat(foundDirector)
                .isPresent()
                .hasValueSatisfying(d -> {
                    assertThat(d.getId()).isEqualTo(createdDirector.getId());
                    assertThat(d.getFirstName()).isEqualTo("Christopher");
                    assertThat(d.getLastName()).isEqualTo("Nolan");
                });
    }

    @Test
    void getDirectors() {
        directorRepository.create(director);

        Director secondDirector = new Director();
        secondDirector.setFirstName("Quentin");
        secondDirector.setLastName("Tarantino");
        directorRepository.create(secondDirector);

        List<Director> directors = directorRepository.getDirectors();

        assertThat(directors)
                .hasSize(2)
                .extracting(Director::getLastName)
                .containsExactlyInAnyOrder("Nolan", "Tarantino");
    }

    @Test
    void findDirectorsByFilmId() {
        Director director1 = directorRepository.create(director);

        Director director2 = new Director();
        director2.setFirstName("Steven");
        director2.setLastName("Spielberg");
        director2 = directorRepository.create(director2);

        Long filmId = 1L;
        jdbc.update("INSERT INTO film (id, name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?, ?)",
                filmId, "Film", "Description", "2000-01-01", 120, 1);

        jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", filmId, director1.getId());
        jdbc.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", filmId, director2.getId());

        List<Director> filmDirectors = directorRepository.findDirectorsByFilmId(filmId);

        assertThat(filmDirectors)
                .hasSize(2)
                .extracting(Director::getId)
                .containsExactlyInAnyOrder(director1.getId(), director2.getId());
    }

    @Test
    void findDirectorById_NotFound() {
        Optional<Director> foundDirector = directorRepository.findDirectorById(999L);
        assertThat(foundDirector).isEmpty();
    }

    @Test
    void findDirectorsByFilmId_NoDirectors() {
        Long filmId = 1L;
        jdbc.update("INSERT INTO film (id, name, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?, ?)",
                filmId, "Film", "Description", "2000-01-01", 120, 1);

        List<Director> filmDirectors = directorRepository.findDirectorsByFilmId(filmId);
        assertThat(filmDirectors).isEmpty();
    }
}