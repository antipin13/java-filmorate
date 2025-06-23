package ru.yandex.practicum.filmorate.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.FilmDto;
import ru.yandex.practicum.filmorate.dal.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dal.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmController {
    final FilmService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findAll() {
        log.info("Запрос на получения списка всех фильмов");
        return filmService.getFilms();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@RequestBody NewFilmRequest filmRequest) {
        log.info("Запрос на добавления фильма {}", filmRequest);
        return filmService.createFilm(filmRequest);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public FilmDto update(@RequestBody UpdateFilmRequest updateFilmRequest) {
        return filmService.updateFilm(updateFilmRequest);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<FilmDto> findById(@PathVariable Long id) {
        return Optional.ofNullable(filmService.getFilmById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean delete(@PathVariable Long id) {
        return filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id,
                        @PathVariable("user-id") Long userId) {
        filmService.addLike(id, userId);

    }

    @DeleteMapping("/{id}/like/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable Long id,
                           @PathVariable("user-id") Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public Collection<FilmDto> findCountFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}