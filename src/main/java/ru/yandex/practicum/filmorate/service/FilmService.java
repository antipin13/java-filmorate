package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.dto.FilmDto;
import ru.yandex.practicum.filmorate.dal.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dal.dto.RatingDto;
import ru.yandex.practicum.filmorate.dal.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
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

    public FilmService(@Qualifier("dbStorage") FilmStorage filmStorage, @Qualifier("dbStorage") UserStorage userStorage,
                       RatingService ratingService, GenreService genreService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingService = ratingService;
        this.genreService = genreService;
    }

    public FilmDto createFilm(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        film.setGenres(request.getGenres());

        Film.validateFilm(film);

        film = filmStorage.create(film);

        return FilmMapper.mapToFilmDto(film);
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
        film.setGenres(genres);

        return FilmMapper.mapToFilmDto(film);
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms()
                .stream()
                .map(FilmMapper::mapToFilmDto)
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

        List<Genre> genres = genreService.getGenresForFilm(filmId);
        existingFilm.setGenres(genres);

        existingFilm = filmStorage.update(existingFilm);

        return FilmMapper.mapToFilmDto(existingFilm);
    }

    public List<FilmDto> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count)
                .stream()
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

    public boolean deleteFilmAndRelations(Long id) {
        // Проверка существования фильма
        Optional<Film> filmOpt = filmStorage.getFilmById(id);
        if (filmOpt.isEmpty()) {
            throw new NotFoundException("Фильм не найден с ID: " + id);
        }
        // Вызов метода репозитория для удаления связанной информации и фильма
        return ((FilmRepository) filmStorage).deleteFilmWithRelations(id);
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
}