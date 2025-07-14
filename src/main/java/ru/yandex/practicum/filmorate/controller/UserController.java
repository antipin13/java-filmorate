package ru.yandex.practicum.filmorate.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserController {
    final UserService userService;
    final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserDto> findAll() {
        log.info("Запрос на получения списка всех пользователей");
        return userService.getUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody NewUserRequest request) {
        log.info("Запрос на добавление пользователя {}", request);
        return userService.createUser(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public UserDto update(@RequestBody UpdateUserRequest request) {
        log.info("Запрос на обновление пользователя {}", request);
        return userService.updateUser(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<UserDto> findById(@PathVariable Long id) {
        return Optional.ofNullable(userService.getUserById(id));
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Void> deleteUser(@PathVariable ("user-id") Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/friends/{friend-id}")
    @ResponseStatus(HttpStatus.OK)
    public UserDto addFriend(@PathVariable Long id,
                             @PathVariable("friend-id") Long friendId) {

        return userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getUserFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    @DeleteMapping("/{id}/friends/{friend-id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeFriend(@PathVariable Long id,
                             @PathVariable("friend-id") Long friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{friend-id}")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> getCommonFriends(@PathVariable Long id,
                                          @PathVariable("friend-id") Long friendId) {
        return userService.getCommonFriend(id, friendId);
    }

    @GetMapping("/{id}/recommendations")
    @ResponseStatus(HttpStatus.OK)
    public List<FilmDto> getRecommendations(@PathVariable Long id) {
        return userService.getRecommendations(id);
    }

    @GetMapping("/{id}/feed")
    @ResponseStatus(HttpStatus.OK)
    public List<EventDto> getFeeds(@PathVariable Long id) {
        return eventService.getEventsByUserId(id);
    }
}
