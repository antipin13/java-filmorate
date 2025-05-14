package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createValidUser() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        assertNotNull(createdUser);
        assertEquals(1, createdUser.getId(), "Пользователь 1 не был создан");
        assertEquals(user.getEmail(), createdUser.getEmail(), "Email пользователя 1 не совпадает");
        assertEquals(user.getLogin(), createdUser.getLogin(), "Логин пользователя 1 не совпадает");
        assertEquals(user.getName(), createdUser.getName(), "Имя пользователя 1 не совпадает");
        assertEquals(user.getBirthday(), createdUser.getBirthday(),
                "Дата рождения пользователя 1 не совпадает");
    }

    @Test
    void createUserNotValidEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Пользователь был создан с пустым email");

        user.setEmail("email.ru");
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Пользователь был создан с некорректным email");
    }

    @Test
    void createUserNotValidLogin() {
        User user = new User();
        user.setEmail("mail@ru");
        user.setLogin("");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Пользователь был создан с пустым логином");

        user.setLogin("login login");
        assertThrows(ValidationException.class, () -> userController.create(user),
                "Пользователь был создан с некорректным логином");
    }

    @Test
    void createUserEmptyName() {
        User user = new User();
        user.setEmail("mail@ru");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createUser = userController.create(user);
        assertEquals(user.getLogin(), createUser.getName(), "Логин и Имя не совпадают");
    }

    @Test
    void createUserNotValidBirthday() {
        User user = new User();
        user.setEmail("mail@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(2026, Month.JUNE, 15));

        assertThrows(ValidationException.class, () -> userController.create(user),
                "Пользователь был создан с датой рождения в будущем");

        user.setBirthday(LocalDate.of(2025, Month.MAY, 11));
        assertEquals(1, userController.create(user).getId(),
                "Пользователь не создался с текущей датой рождения");
    }

    @Test
    void updateValidUser() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("email_email@ru");
        updateUser.setLogin("login_login");
        updateUser.setName("Имя пользователя обновленное");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        User resultUser = userController.update(updateUser);

        assertNotNull(resultUser, "Пользователь не обновился");
        assertEquals(resultUser.getEmail(), updateUser.getEmail(), "Email пользователя не обновился");
        assertEquals(resultUser.getLogin(), updateUser.getLogin(), "Логин пользователя не обновился");
        assertEquals(resultUser.getName(), updateUser.getName(), "Имя пользователя не обновилось");
        assertEquals(resultUser.getBirthday(), updateUser.getBirthday(),
                "Дата рождения пользователя не обновилась");
    }

    @Test
    void updateUserNotID() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setEmail("email_email@ru");
        updateUser.setLogin("login_login");
        updateUser.setName("Имя пользователя обновленное");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        assertThrows(ConditionsNotMetException.class, () -> userController.update(updateUser),
                "Пользователь обновился без указания ID");
    }

    @Test
    void updateUserNotValidID() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(2);
        updateUser.setEmail("email_email@ru");
        updateUser.setLogin("login_login");
        updateUser.setName("Имя пользователя обновленное");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        assertThrows(NotFoundException.class, () -> userController.update(updateUser),
                "Пользователь обновился c несуществующим ID");
    }

    @Test
    void updateUserNotValidEmail() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("email.ru");
        updateUser.setLogin("login_login");
        updateUser.setName("Имя пользователя обновленное");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        assertThrows(ValidationException.class, () -> userController.update(updateUser),
                "Пользователь обновился с невалидным email");
    }

    @Test
    void updateUserNotValidLogin() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("email@ru");
        updateUser.setLogin("");
        updateUser.setName("Имя пользователя обновленное");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        assertThrows(ValidationException.class, () -> userController.update(updateUser),
                "Пользователь обновился с невалидным логином");

        updateUser.setLogin("login login");
        assertThrows(ValidationException.class, () -> userController.update(updateUser),
                "Пользователь обновился с невалидным логином");
    }

    @Test
    void updateUserEmptyName() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("email@ru");
        updateUser.setLogin("login");
        updateUser.setName("");
        updateUser.setBirthday(LocalDate.of(1999, Month.MAY, 15));

        User resultUser = userController.update(updateUser);

        assertEquals(updateUser.getLogin(), resultUser.getName(),
                "Некорректное обновление имени пользователя");
    }

    @Test
    void updateUserNotValidBirthday() {
        User user = new User();
        user.setEmail("email@ru");
        user.setLogin("login");
        user.setName("Имя пользователя 1");
        user.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser = userController.create(user);

        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("email@ru");
        updateUser.setLogin("login");
        updateUser.setName("");
        updateUser.setBirthday(LocalDate.of(2030, Month.MAY, 15));

        assertThrows(ValidationException.class, () -> userController.update(updateUser),
                "Пользователь обновился с невалидной датой рождения");
    }
}