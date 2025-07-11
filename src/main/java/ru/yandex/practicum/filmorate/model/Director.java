package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

@Slf4j
@Data
public class Director {
    Long id;
    String firstName;
    String lastName;

    public static void validateDirector(Director director) {
        if (director.getFirstName().isBlank()) {
            log.warn("Ошибка валидации: Имя режиссера не может быть пустым");
            throw new ValidationException("director.name",
                    "Имя режиссера не может быть пустым");
        }
    }
}
