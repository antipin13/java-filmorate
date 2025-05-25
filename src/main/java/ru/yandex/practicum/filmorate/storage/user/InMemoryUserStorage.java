package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import ru.yandex.practicum.filmorate.model.User;


import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public User create(User user) {
        log.info("Запрос на создание пользователя {}", user);
        User.validateUser(user);

        user.setId(getNextId());

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя установлено по умолчанию {}", user.getName());
        }

        users.put(user.getId(), user);
        log.info("Пользователь с ID {} успешно создан", user.getId());
        return user;
    }

    @Override
    public User update(User newUser) {
        log.info("Запрос на обновление пользователя {}", newUser);
        if (newUser.getId() == null) {
            log.warn("ID пользователя должен быть указан");
            throw new ConditionsNotMetException("ID пользователя должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            User.validateUser(newUser);

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

    @Override
    public User delete(User user) {
        log.info("Запрос на удаление пользователя {}", user);
        if (user.getId() == null) {
            log.warn("ID пользователя должен быть указан");
            throw new ConditionsNotMetException("ID пользователя должен быть указан");
        }

        if (users.containsKey(user.getId())) {
            users.remove(user.getId());
        }

        log.warn("Фильм с ID {} не найден", user.getId());
        throw new NotFoundException(String.format("Пользователь с ID = %d не найден", user.getId()));
    }

    @Override
    public Integer getNextId() {
        Integer currentMaxId = users.keySet()
                .stream()
                .mapToInt(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public User getUserById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("Пользователь с ID = %d не найден", id));
        }
        return users.get(id);
    }
}
