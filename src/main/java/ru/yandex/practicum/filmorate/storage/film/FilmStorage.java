package ru.yandex.practicum.filmorate.storage.film;

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

    List<Long> getLikedFilmsByUser(Long userId);

    List<Long> getUsersLikedSameFilms(List<Long> filmsIds, Long ownUserId);

    List<Long> getRecommendedFilmIds(Long userId, Long similarUserId);
}
