package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmRowMapper.class, GenreRepository.class, GenreRowMapper.class,
        RatingRepository.class, RatingRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class GenreRepositoryTest {
    final GenreRepository genreRepository;
    final FilmRepository filmRepository;
    final RatingRepository ratingRepository;

    @Test
    void getGenreById() {
        Optional<Genre> genre = genreRepository.findById(1L);

        assertThat(genre)
                .isPresent()
                .hasValueSatisfying(genre1 ->
                        assertThat(genre1).hasFieldOrPropertyWithValue("name", "Комедия")
                );
    }

    @Test
    void getAllGenres() {
        List<Genre> genres = genreRepository.findAll();

        assertThat(genres.size()).isEqualTo(6);
    }

    @Test
    void getGenresByFilms() {
        Optional<Genre> genre1 = genreRepository.findById(1L);
        Optional<Genre> genre2 = genreRepository.findById(2L);

        List<Genre> genres = new ArrayList<>();
        genres.add(genre1.get());
        genres.add(genre2.get());

        Optional<Rating> mpa = ratingRepository.findById(1L);

        Film film = Film.builder()
                .name("film1")
                .description("description 1")
                .releaseDate(LocalDate.of(2007, 9, 13))
                .duration(120)
                .mpa(mpa.get())
                .genres(genres)
                .build();

        Film createFilm = filmRepository.create(film);

        List<Genre> filmGenres = genreRepository.findGenresByFilmId(createFilm.getId());

        assertThat(filmGenres.size()).isEqualTo(2);
    }
}