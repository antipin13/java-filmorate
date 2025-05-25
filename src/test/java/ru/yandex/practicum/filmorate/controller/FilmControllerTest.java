package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;
    private InMemoryFilmStorage inMemoryFilmStorage;
    private FilmService filmService;
    private InMemoryUserStorage inMemoryUserStorage;

    @BeforeEach
    void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();
        inMemoryFilmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(inMemoryFilmStorage);
        filmController = new FilmController(inMemoryFilmStorage, filmService, inMemoryUserStorage);
    }

    @Test
    void createValidFilm() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId(), "Фильм 1 не был создан");
        assertEquals(film.getName(), createdFilm.getName(), "Имя фильма 1 не совпадает");
        assertEquals(film.getDescription(), createdFilm.getDescription(), "Описание фильма 1 не совпадает");
        assertEquals(film.getReleaseDate(), createdFilm.getReleaseDate(), "Дата релиза фильма 1 не совпадает");
        assertEquals(120, createdFilm.getDuration(), "Длительность фильма 1 не совпадает");
    }

    @Test
    void createFilmNotValidName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм был создан без названия");

        film.setName(" ");
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм был создан без названия");
    }

    @Test
    void createFilmNotValidDescription() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("!".repeat(201));
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм создался с описанием более 200 символов");

        film.setDescription("!".repeat(200));
        assertNotNull(filmController.create(film), "Фильм ровно с 200 символами не создался");
    }

    @Test
    void createFilmNotValidReleaseDate() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1800, Month.DECEMBER, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм создался с датой ранее 28.12.1895");

        film.setReleaseDate(LocalDate.of(1895, Month.DECEMBER, 28));
        assertNotNull(filmController.create(film), "Фильм с датой 28.12.1895 не создался");
    }

    @Test
    void createFilmNotValidDuration() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1900, Month.DECEMBER, 1));
        film.setDuration(-120);

        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм создался с отрицательной длительностью");

        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.create(film),
                "Фильм создался с нулевой длительностью");
    }

    @Test
    void updateValidFilm() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(createdFilm.getId());
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(2013, Month.APRIL, 13));
        updateFilm.setDuration(133);

        Film resultFilm = filmController.update(updateFilm);

        assertNotNull(resultFilm, "Фильм не обновился");
        assertEquals(resultFilm.getName(), updateFilm.getName(), "Название фильма не обновилось");
        assertEquals(resultFilm.getDescription(), updateFilm.getDescription(),
                "Описание фильма не обновилось");
        assertEquals(resultFilm.getReleaseDate(), updateFilm.getReleaseDate(),
                "Дата релиза фильма не обновилась");
        assertEquals(resultFilm.getDuration(), updateFilm.getDuration(), "Длительность фильма не обновилась");
    }

    @Test
    void updateFilmNotID() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(2013, Month.APRIL, 13));
        updateFilm.setDuration(133);

        assertThrows(ConditionsNotMetException.class, () -> filmController.update(updateFilm),
                "Фильм обновился без указания ID");
    }

    @Test
    void updateFilmNotValidID() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(2);
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(2013, Month.APRIL, 13));
        updateFilm.setDuration(133);

        assertThrows(NotFoundException.class, () -> filmController.update(updateFilm),
                "Фильм обновился c несуществующим ID");
    }

    @Test
    void updateFilmNotValidName() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(1);
        updateFilm.setName("");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(2013, Month.APRIL, 13));
        updateFilm.setDuration(133);

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm),
                "Фильм обновился c невалидным именем");
    }

    @Test
    void updateFilmNotValidDescription() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(1);
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("!".repeat(201));
        updateFilm.setReleaseDate(LocalDate.of(2013, Month.APRIL, 13));
        updateFilm.setDuration(133);

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm),
                "Фильм обновился c невалидным описанием");
    }

    @Test
    void updateFilmNotValidDateRelease() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(1);
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(1800, Month.APRIL, 13));
        updateFilm.setDuration(133);

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm),
                "Фильм обновился c невалидной датой релиза");
    }

    @Test
    void updateFilmNotValidDuration() {
        Film film = new Film();
        film.setName("Фильм 1");
        film.setDescription("Описание фильма 1");
        film.setReleaseDate(LocalDate.of(1999, Month.DECEMBER, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        Film updateFilm = new Film();
        updateFilm.setId(1);
        updateFilm.setName("Обновленное имя");
        updateFilm.setDescription("Обновленное описание");
        updateFilm.setReleaseDate(LocalDate.of(1900, Month.APRIL, 13));
        updateFilm.setDuration(-133);

        assertThrows(ValidationException.class, () -> filmController.update(updateFilm),
                "Фильм обновился c невалидной длительностью");
    }
}