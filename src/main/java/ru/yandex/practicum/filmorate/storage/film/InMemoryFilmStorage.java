package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.info("Запрос на создание фильма {}", film);
        Film.validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм с ID - {} успешно создан", film.getId());
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        log.info("Запрос на обновление фильма {}", newFilm);
        if (newFilm.getId() == null) {
            log.warn("ID фильма должен быть указан");
            throw new ConditionsNotMetException("ID фильма должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            Film.validateFilm(newFilm);

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

    @Override
    public Film delete(Film film) {
        log.info("Запрос на удаление фильма {}", film);
        if (film.getId() == null) {
            log.warn("ID фильма должен быть указан");
            throw new ConditionsNotMetException("ID фильма должен быть указан");
        }

        if (films.containsKey(film.getId())) {
            films.remove(film.getId());
        }

        log.warn("Фильм с ID {} не найден", film.getId());
        throw new NotFoundException(String.format("Фильм с ID = %d не найден", film.getId()));
    }

    @Override
    public Integer getNextId() {
        Integer currentMaxId = films.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Film getFilmById(Integer id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException(String.format("Фильм с ID = %d не найден", id));
        }
        return films.get(id);
    }
}
