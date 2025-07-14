package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.SearchBy;
import ru.yandex.practicum.filmorate.controller.SortBy;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmService {
    final FilmStorage filmStorage;
    final UserStorage userStorage;
    final RatingService ratingService;
    final GenreService genreService;
    final DirectorService directorService;

    public FilmService(@Qualifier("dbStorage") FilmStorage filmStorage, @Qualifier("dbStorage") UserStorage userStorage,
                       RatingService ratingService, GenreService genreService, DirectorService directorService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingService = ratingService;
        this.genreService = genreService;
        this.directorService = directorService;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        film.setGenres(request.getGenres());
        film.setDirectors(request.getDirectors());
        Film.validateFilm(film);
        ratingService.getRatingById(film.getMpa().getId());
        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreService.getGenreById(genre.getId()));
        }
        if (film.getDirectors() != null) {
            film.getDirectors().forEach(director -> directorService.getDirectorById(director.getId()));
        }
        film = filmStorage.create(film);
        return getFilmById(film.getId());
    }

    public FilmDto getFilmById(Long filmId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден с ID: " + filmId));

        RatingDto mpaDto = ratingService.getRatingById(film.getMpa().getId());
        Rating mpa = new Rating();
        mpa.setId(mpaDto.getId());
        mpa.setName(mpaDto.getName());
        film.setMpa(mpa);

        List<Genre> genres = genreService.getGenresForFilm(filmId);
        Set<Director> directors = directorService.getDirectorsByFilmId(filmId);
        film.setGenres(genres);
        film.setDirectors(directors);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms()
                .stream()
                .map(film -> {
                    List<Genre> genres = genreService.getGenresForFilm(film.getId());
                    Set<Director> directors = directorService.getDirectorsByFilmId(film.getId());
                    film.setGenres(genres);
                    film.setDirectors(directors);
                    return FilmMapper.mapToFilmDto(film);
                })
                .collect(Collectors.toList());
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        Long filmId = request.getId();

        Film existingFilm = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с ID - %d не найден", filmId)));

        existingFilm = FilmMapper.updateFilmFields(existingFilm, request);

        RatingDto mpaDto = ratingService.getRatingById(existingFilm.getMpa().getId());
        Rating mpa = new Rating();
        mpa.setId(mpaDto.getId());
        mpa.setName(mpaDto.getName());
        existingFilm.setMpa(mpa);

        if (request.getGenres() != null) {
            existingFilm.getGenres().forEach(genre -> genreService.getGenreById(genre.getId()));
            existingFilm.setDirectors(request.getDirectors());
        }

        if (request.getDirectors() != null) {
            existingFilm.getDirectors().forEach(director -> directorService.getDirectorById(director.getId()));
            existingFilm.setGenres(request.getGenres());
        }

        filmStorage.update(existingFilm);

        return getFilmById(filmId);
    }

    public List<FilmDto> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public boolean deleteFilm(Long id) {
        Optional<Film> filmOpt = filmStorage.getFilmById(id);
        if (filmOpt.isPresent()) {
            return filmStorage.delete(filmOpt.get());
        } else {
            throw new NotFoundException(String.format("Фильм с ID - %d не найден", id));
        }
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.getFilmById(filmId);
        userStorage.getUserById(userId);

        filmStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> findFilmsByDirectorId(Long directorId, SortBy sortBy) {
        directorService.getDirectorById(directorId);
        return filmStorage.getFilmsByDirectorId(directorId, sortBy).stream()
                .map(film -> {
                    List<Genre> genres = genreService.getGenresForFilm(film.getId());
                    Set<Director> directors = directorService.getDirectorsByFilmId(film.getId());
                    film.setGenres(genres);
                    film.setDirectors(directors);
                    return FilmMapper.mapToFilmDto(film);
                })
                .collect(Collectors.toList());
    }

    public List<FilmDto> getFilmsByQuery(String query, List<SearchBy> searchBys) {
        return ((FilmRepository) filmStorage).getFilmsByQuery(query, searchBys).stream()
                .map(film -> {
                    List<Genre> genres = genreService.getGenresForFilm(film.getId());
                    Set<Director> directors = directorService.getDirectorsByFilmId(film.getId());
                    film.setGenres(genres);
                    film.setDirectors(directors);
                    return FilmMapper.mapToFilmDto(film);
                })
                .collect(Collectors.toList());
    }
}