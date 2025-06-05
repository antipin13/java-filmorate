package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
    private UserController userController;
    private InMemoryUserStorage inMemoryUserStorage;
    private UserService userService;


    @BeforeEach
    void setUp() {
        inMemoryUserStorage = new InMemoryUserStorage();
        userService = new UserService(inMemoryUserStorage);
        userController = new UserController(inMemoryUserStorage, userService);
    }

    @Test
    void addFriend() {
        User user1 = new User();
        user1.setEmail("email@ru");
        user1.setLogin("login");
        user1.setName("Имя пользователя 1");
        user1.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser1 = userController.create(user1);

        User user2 = new User();
        user2.setEmail("email@ru");
        user2.setLogin("login2");
        user2.setName("Имя пользователя 2");
        user2.setBirthday(LocalDate.of(1999, Month.JUNE, 15));

        User createdUser2 = userController.create(user2);

        userService.addFriend(user1, user2);

        assertTrue(user1.getFriends().contains(2));
    }

}