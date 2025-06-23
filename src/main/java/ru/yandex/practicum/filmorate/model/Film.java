package ru.yandex.practicum.filmorate.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;


@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Builder
@AllArgsConstructor
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Rating mpa;
    List<Genre> genres = new ArrayList<>();

    public Film() {}

    public static void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: Название не может быть пустым");
            throw new ValidationException(Optional.ofNullable(film.getName()).toString(),
                    "Название не может быть пустым");
        }

        if (film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: Объем описания не более 200 символов");
            throw new ValidationException(film.getDescription(), "Объем описания не более 200 символов");
        }

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
            log.warn("Ошибка валидации: Дата релиза должна быть не ранее 28.12.1895");
            throw new ValidationException(film.getReleaseDate().toString(), "Дата релиза должна быть не ранее 28.12.1895");
        }

        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: Длительность фильма не может быть отрицательной или равной нулю");
            throw new ValidationException(film.getDuration().toString(), "Длительность фильма не может быть отрицательной или равной нулю");
        }
    }
}
