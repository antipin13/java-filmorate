package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение списка всех пользователей {}", users);
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Запрос на создание пользователя {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации: Email не может быть пустым или не содержать символ - @");
            throw new ValidationException("Email не может быть пустым или не содержать символ - @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: Логин не может быть пустым или содержать пробелы");
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: Дата рождения не может быть в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя установлено по умолчанию {}", user.getName());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно создан", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("Запрос на обновление пользователя {}", newUser);
        if (newUser.getId() == null) {
            log.warn("ID должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (newUser.getEmail() == null || newUser.getEmail().isBlank() || !newUser.getEmail().contains("@")) {
                log.warn("Ошибка валидации: Email не может быть пустым или не содержать символ - @");
                throw new ValidationException("Email не может быть пустым или не содержать символ - @");
            }

            if (newUser.getLogin() == null || newUser.getLogin().isBlank() || newUser.getLogin().contains(" ")) {
                log.warn("Ошибка валидации: Логин не может быть пустым или содержать пробелы");
                throw new ValidationException("Логин не может быть пустым или содержать пробелы");
            }

            if (newUser.getBirthday().isAfter(LocalDate.now())) {
                log.warn("Ошибка валидации: Дата рождения не может быть в будущем");
                throw new ValidationException("Дата рождения не может быть в будущем");
            }

            oldUser.setEmail(newUser.getEmail());
            oldUser.setBirthday(newUser.getBirthday());
            oldUser.setLogin(newUser.getLogin());

            if (newUser.getName() == null || newUser.getName().isBlank()) {
                oldUser.setName(newUser.getLogin());
                log.info("Имя пользователя установлено по умолчанию {}", oldUser.getName());
            } else {
                oldUser.setName(newUser.getName());
            }
            log.info("Пользователь с ID {} успешно обновлен", newUser.getId());

            return oldUser;
        }
        log.warn("Пользователь с ID {} не найден", newUser.getId());
        throw new NotFoundException(String.format("Пользователь с ID = %d не найден", newUser.getId()));
    }

    private Integer getNextId() {
        Integer currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
