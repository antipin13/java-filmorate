package ru.yandex.practicum.filmorate.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreController {
    final GenreService genreService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<GenreDto> findAll() {
        log.info("Запрос на получения списка всех жанров");
        return genreService.getGenres();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<GenreDto> findById(@PathVariable Long id) {
        log.info("Запрос на получения жанра с ID - {}", id);
        return Optional.ofNullable(genreService.getGenreById(id));
    }
}
