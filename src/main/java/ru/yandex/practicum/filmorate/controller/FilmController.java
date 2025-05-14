package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Запрос на получения списка всех фильмов {}", films);
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Запрос на создание фильма {}", film);
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: Название не может быть пустым");
            throw new ValidationException("Название не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: Объем описания не более 200 символов");
            throw new ValidationException("Объем описания не более 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Ошибка валидации: Дата релиза должна быть не ранее 28.12.1895");
            throw new ValidationException("Дата релиза должна быть не ранее 28.12.1895");
        }

        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: Длительность фильма не может быть отрицательной или равной нулю");
            throw new ValidationException("Длительность фильма не может быть отрицательной или равной нулю");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм с ID - {} успешно создан", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("Запрос на обновление фильма {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("ID должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            if (newFilm.getName() == null || newFilm.getName().isBlank()) {
                log.warn("Ошибка валидации: Название не может быть пустым");
                throw new ValidationException("Название не может быть пустым");
            }

            if (newFilm.getDescription().length() > 200) {
                log.warn("Ошибка валидации: Объем описания не более 200 символов");
                throw new ValidationException("Объем описания не более 200 символов");
            }

            if (newFilm.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
                log.warn("Ошибка валидации: Дата релиза должна быть не ранее 28.12.1895");
                throw new ValidationException("Дата релиза должна быть не ранее 28.12.1895");
            }

            if (newFilm.getDuration() <= 0) {
                log.warn("Ошибка валидации: Длительность фильма не может быть отрицательной или равной нулю");
                throw new ValidationException("Длительность фильма не может быть отрицательной или равной нулю");
            }

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            log.info("Фильм с ID {} успешно обновлен", newFilm.getId());

            return oldFilm;
        }
        log.warn("Фильм с ID {} не найден", newFilm.getId());
        throw new NotFoundException(String.format("Фильм с ID = %d не найден", newFilm.getId()));
    }

    private Integer getNextId() {
        Integer currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}