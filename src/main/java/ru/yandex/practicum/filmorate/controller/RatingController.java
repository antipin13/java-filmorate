package ru.yandex.practicum.filmorate.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.RatingDto;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingController {
    final RatingService ratingService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<RatingDto> findAll() {
        log.info("Запрос на получения списка всех рейтингов");
        return ratingService.getRatings();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<RatingDto> findById(@PathVariable Long id) {
        log.info("Запрос на получения рейтинга с ID - {}", id);
        return Optional.ofNullable(ratingService.getRatingById(id));
    }
}
