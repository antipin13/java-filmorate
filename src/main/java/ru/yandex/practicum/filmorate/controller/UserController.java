package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final InMemoryUserStorage inMemoryUserStorage;
    private final UserService userService;

    @Autowired
    public UserController(InMemoryUserStorage inMemoryUserStorage, UserService userService) {
        this.inMemoryUserStorage = inMemoryUserStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение списка всех пользователей {}", inMemoryUserStorage.getUsers());
        return inMemoryUserStorage.getUsers().values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        return inMemoryUserStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        return inMemoryUserStorage.update(newUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User delete(@PathVariable Integer id) {
        return inMemoryUserStorage.delete(inMemoryUserStorage.getUserById(id));
    }

    @PutMapping("/{id}/friends/{friend-id}")
    @ResponseStatus(HttpStatus.OK)
    public User addFriend(@PathVariable Integer id,
                        @PathVariable("friend-id") Integer friendId) {
        userService.addFriend(inMemoryUserStorage.getUserById(id), inMemoryUserStorage.getUserById(friendId));
        return inMemoryUserStorage.getUserById(id);
    }

    @DeleteMapping("/{id}/friends/{friend-id}")
    @ResponseStatus(HttpStatus.OK)
    public User deleteFriend(@PathVariable Integer id,
                             @PathVariable("friend-id") Integer friendId) {
        userService.deleteFriend(inMemoryUserStorage.getUserById(id), inMemoryUserStorage.getUserById(friendId));
        return inMemoryUserStorage.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUserFriends(@PathVariable Integer id) {
        return userService.getUserFriends(inMemoryUserStorage.getUserById(id));
    }

    @GetMapping("/{id}/friends/common/{other-id}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getCommonFriends(@PathVariable Integer id,
                                       @PathVariable("other-id") Integer otherId) {
        return userService.getCommonFriends(inMemoryUserStorage.getUserById(id),
                inMemoryUserStorage.getUserById(otherId));
    }
}
