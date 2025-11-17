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
import ru.yandex.practicum.filmorate.controller.SearchBy;
import ru.yandex.practicum.filmorate.controller.SortBy;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmRepository.class, FilmRowMapper.class, RatingRepository.class, GenreRepository.class,
        RatingRowMapper.class, GenreRowMapper.class, UserRepository.class, UserRowMapper.class,
        DirectorRepository.class, DirectorRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class FilmRepositoryTest {
    final JdbcTemplate jdbc;
    final FilmRepository filmRepository;
    final UserRepository userRepository;
    final DirectorRepository directorRepository;
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

    @Test
    void getFilmsByDirectorId_SortedByYear() {
        Director director = new Director();
        director.setFirstName("Director Name");
        director = directorRepository.create(director);

        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(1990, 1, 1))
                .duration(130)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(140)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        filmRepository.create(film1);
        filmRepository.create(film2);
        filmRepository.create(film3);

        List<Film> films = filmRepository.getFilmsByDirectorId(director.getId(), SortBy.YEAR);

        Assertions.assertThat(films).hasSize(3);
        Assertions.assertThat(films.get(0).getName()).isEqualTo("Film 2");
        Assertions.assertThat(films.get(1).getName()).isEqualTo("Film 1");
        Assertions.assertThat(films.get(2).getName()).isEqualTo("Film 3");
    }

    @Test
    void getFilmsByDirectorId_SortedByLikes() {
        Director director = new Director();
        director.setFirstName("Director Name");
        director = directorRepository.create(director);

        User user1 = User.builder()
                .email("user1@mail.ru")
                .login("login1")
                .name("name1")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user1 = userRepository.create(user1);

        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        user2 = userRepository.create(user2);

        Film film1 = Film.builder()
                .name("Film 1")
                .description("Description 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        Film film2 = Film.builder()
                .name("Film 2")
                .description("Description 2")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(130)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        Film film3 = Film.builder()
                .name("Film 3")
                .description("Description 3")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(140)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();

        film1 = filmRepository.create(film1);
        film2 = filmRepository.create(film2);
        film3 = filmRepository.create(film3);

        filmRepository.addLike(film1.getId(), user1.getId());
        filmRepository.addLike(film1.getId(), user2.getId());
        filmRepository.addLike(film2.getId(), user1.getId());

        List<Film> films = filmRepository.getFilmsByDirectorId(director.getId(), SortBy.LIKES);

        Assertions.assertThat(films).hasSize(3);
        Assertions.assertThat(films.get(0).getName()).isEqualTo("Film 1");
        Assertions.assertThat(films.get(1).getName()).isEqualTo("Film 2");
        Assertions.assertThat(films.get(2).getName()).isEqualTo("Film 3");
    }

    @Test
    void getFilmsByDirectorId_NoFilms_ReturnsEmptyList() {
        Director director = new Director();
        director.setFirstName("Director Name");
        director = directorRepository.create(director);

        List<Film> films = filmRepository.getFilmsByDirectorId(director.getId(), SortBy.YEAR);

        Assertions.assertThat(films).isEmpty();
    }

    @Test
    void getFilmsByQuery_shouldFindByTitleAndDirector() {
        Director director1 = new Director();
        director1.setFirstName("Квентин");
        director1.setLastName("Тарантино");
        director1 = directorRepository.create(director1);

        Director director2 = new Director();
        director2.setFirstName("Кристофер");
        director2.setLastName("Нолан");
        director2 = directorRepository.create(director2);

        Film film1 = Film.builder()
                .name("Криминальное чтиво")
                .description("Фильм про гангстеров")
                .releaseDate(LocalDate.of(1994, 10, 14))
                .duration(154)
                .mpa(mpa)
                .directors(Set.of(director1))
                .build();

        Film film2 = Film.builder()
                .name("Начало")
                .description("Фильм про сны")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(mpa)
                .directors(Set.of(director2))
                .build();

        filmRepository.create(film1);
        filmRepository.create(film2);

        List<Film> result = filmRepository.getFilmsByQuery("кри", List.of(SearchBy.TITLE, SearchBy.DIRECTOR));

        Assertions.assertThat(result).hasSize(2)
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Криминальное чтиво", "Начало");
    }

    @Test
    void getFilmsByQuery_shouldFindByTitleOnly() {
        Film film = Film.builder()
                .name("Побег из Шоушенка")
                .description("Драма про заключенного")
                .releaseDate(LocalDate.of(1994, 9, 23))
                .duration(142)
                .mpa(mpa)
                .build();
        filmRepository.create(film);

        List<Film> result = filmRepository.getFilmsByQuery("шоушенк", List.of(SearchBy.TITLE));

        Assertions.assertThat(result).hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Побег из Шоушенка");
    }

    @Test
    void getFilmsByQuery_shouldFindByDirectorOnly() {
        Director director = new Director();
        director.setFirstName("Стивен");
        director.setLastName("Спилберг");
        director = directorRepository.create(director);

        Film film = Film.builder()
                .name("Парк Юрского периода")
                .description("Про динозавров")
                .releaseDate(LocalDate.of(1993, 6, 11))
                .duration(127)
                .mpa(mpa)
                .directors(Set.of(director))
                .build();
        filmRepository.create(film);

        List<Film> result = filmRepository.getFilmsByQuery("Стивен", List.of(SearchBy.DIRECTOR));

        Assertions.assertThat(result).hasSize(1)
                .extracting(Film::getName)
                .containsExactly("Парк Юрского периода");
    }
}