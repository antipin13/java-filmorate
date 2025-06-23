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
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmRowMapper.class, RatingRepository.class, GenreRepository.class,
        RatingRowMapper.class, GenreRowMapper.class, UserRepository.class, UserRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class FilmRepositoryTest {
    final JdbcTemplate jdbc;
    final FilmRepository filmRepository;
    final UserRepository userRepository;
    Film film;
    Rating mpa = new Rating();

    @BeforeEach
    void setUp() {
        mpa.setId(1L);
        mpa.setName("mpa");

        film = Film.builder()
                .name("film1")
                .description("description 1")
                .releaseDate(LocalDate.of(2007, 8, 13))
                .duration(120)
                .mpa(mpa)
                .build();
    }

    @AfterEach
    void afterEach() {
        mpa.setId(1L);
        mpa.setName("mpa");

        film = Film.builder()
                .name("film1")
                .description("description 1")
                .releaseDate(LocalDate.of(2007, 9, 13))
                .duration(120)
                .mpa(mpa)
                .build();
    }

    @Test
    void createFilm() {
        Film createFilm = filmRepository.create(film);

        assertThat(createFilm)
                .hasFieldOrPropertyWithValue("id", createFilm.getId());
    }

    @Test
    void updateFilm() {
        Film createFilm = filmRepository.create(film);

        Film updateFilm = Film.builder()
                .id(createFilm.getId())
                .name("updateFilm")
                .description("Update description 1")
                .releaseDate(LocalDate.of(2007, 9, 27))
                .duration(140)
                .mpa(mpa)
                .build();

        Film updateFilmInDb = filmRepository.update(updateFilm);

        assertThat(updateFilmInDb)
                .hasFieldOrPropertyWithValue("name", "updateFilm");
    }

    @Test
    public void findFilmById() {
        Film createFilm = filmRepository.create(film);

        Optional<Film> filmOptional = filmRepository.getFilmById(createFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film1 ->
                        assertThat(film1).hasFieldOrPropertyWithValue("id", filmOptional.get().getId())
                );
    }

    @Test
    public void deleteFilm() {
        Film deleteFilm = filmRepository.create(film);

        boolean delete = filmRepository.delete(deleteFilm);

        assertThat(delete).isEqualTo(true);
    }

    @Test
    public void getUsers() {
        filmRepository.create(film);

        Film film2 = Film.builder()
                .name("film2")
                .description("description 2")
                .releaseDate(LocalDate.of(2004, 9, 13))
                .duration(130)
                .mpa(mpa)
                .build();

        filmRepository.create(film2);

        List<Film> films = filmRepository.getFilms();

        assertThat(films.size()).isEqualTo(2);
    }

    @Test
    public void addRemoveLike() {
        User user = User.builder()
                .id(null)
                .email("user@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1999,06,15))
                .build();

        User createUser = userRepository.create(user);

        Film createFilm = filmRepository.create(film);

        filmRepository.addLike(createFilm.getId(), createUser.getId());

        String filmLikes = "SELECT COUNT(*) FROM likes WHERE film_id = ?";

        Integer count = jdbc.queryForObject(filmLikes, Integer.class, createFilm.getId());

        assertThat(count.equals(1));

        filmRepository.removeLike(createFilm.getId(), createUser.getId());

        count = jdbc.queryForObject(filmLikes, Integer.class, createFilm.getId());

        assertThat(count.equals(1));
    }

    @Test
    void getPopularFilms() {
        User user = User.builder()
                .id(null)
                .email("user@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1999,06,15))
                .build();
        User createUser1 = userRepository.create(user);

        Film createFilm1 = filmRepository.create(film);

        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(1999,06,15))
                .build();
        User createUser2 = userRepository.create(user2);

        Film film2 = Film.builder()
                .name("film2")
                .description("description 2")
                .releaseDate(LocalDate.of(2004, 9, 13))
                .duration(130)
                .mpa(mpa)
                .build();
        Film createFilm2 = filmRepository.create(film2);

        filmRepository.addLike(createFilm1.getId(), createUser1.getId());
        filmRepository.addLike(createFilm1.getId(), createUser2.getId());
        filmRepository.addLike(createFilm2.getId(), createUser2.getId());

        List<Film> popularFilms = filmRepository.getPopularFilms(1);

        assertThat(popularFilms.size()).isEqualTo(1);
    }
}