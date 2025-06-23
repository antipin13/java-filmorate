package ru.yandex.practicum.filmorate.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.time.LocalDate;
import java.util.*;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Builder
public class User {
    Long id;
    String email;
    String login;
    String name;
    LocalDate birthday;
    List<User> friends;

    public static void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: Email не может быть пустым или не содержать символ - @");
            throw new ValidationException(user.getEmail(), "Email не может быть пустым или не содержать символ - @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: Логин не может быть пустым или содержать пробелы");
            throw new ValidationException(Optional.ofNullable(user.getLogin()).toString(),
                    "Логин не может быть пустым или содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя установлено по умолчанию {}", user.getName());
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: Дата рождения не может быть в будущем");
            throw new ValidationException(user.getBirthday().toString(), "Дата рождения не может быть в будущем");
        }
    }
}
