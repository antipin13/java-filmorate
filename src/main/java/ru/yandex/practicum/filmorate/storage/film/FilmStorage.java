package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.controller.SortBy;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    boolean delete(Film film);

    Optional<Film> getFilmById(Long id);

    List<Film> getFilms();

    List<Film> getPopularFilms(Integer countFilms);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getFilmsByDirectorId(Long directorId, SortBy sortBy);
}
