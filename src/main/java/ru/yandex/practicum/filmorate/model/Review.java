package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Slf4j
public class Review {
    Long reviewId;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;

    public static void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            log.warn("Ошибка валидации: поле content не может быть пустым");
            throw new ValidationException(review.getContent(), "поле content не может быть пустым");
        }

        if (review.getIsPositive() == null) {
            log.warn("Ошибка валидации: поле isPositive не может быть пустым");
            throw new ValidationException(review.getIsPositive().toString(), "поле isPositive не может быть пустым");
        }

        if (review.getUserId() <= 0) {
            log.warn("Ошибка валидации: поле userId не может быть меньше или равно 0");
            throw new NotFoundException("Некорректное значение ID пользователя");
        }

        if (review.getUserId() == null) {
            log.warn("Ошибка валидации: поле userId не может быть пустым");
            throw new ValidationException(review.getUserId().toString(), "Некорректное значение ID пользователя");
        }

        if (review.getFilmId() <= 0) {
            log.warn("Ошибка валидации: поле filmId не может быть пустым или быть меньше или равно 0");
            throw new NotFoundException("Некорректное значение ID фильма");
        }

        if (review.getFilmId() == null) {
            log.warn("Ошибка валидации: поле filmId не может быть пустым");
            throw new ValidationException(review.getFilmId().toString(), "Некорректное значение ID фильма");
        }
    }
}
