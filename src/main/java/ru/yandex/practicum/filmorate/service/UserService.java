package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.exceptions.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserService {
    final UserStorage userStorage;
    final FriendshipRepository friendshipRepository;
    final FilmStorage filmStorage;

    public UserService(@Qualifier("dbStorage") UserStorage userStorage, FriendshipRepository friendshipRepository,
                       @Qualifier("dbStorage") FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.friendshipRepository = friendshipRepository;
        this.filmStorage = filmStorage;
    }

    public UserDto createUser(NewUserRequest request) {
        User user = UserMapper.mapToUser(request);

        User.validateUser(user);

        user = userStorage.create(user);

        return UserMapper.mapToUserDto(user);
    }

    public UserDto getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers()
                .stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto updateUser(UpdateUserRequest request) {
        Long userId = request.getId();

        User existingUser = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        existingUser = UserMapper.updateUserFields(existingUser, request);

        existingUser = userStorage.update(existingUser);

        return UserMapper.mapToUserDto(existingUser);
    }

    public void deleteUser(Long userId) {
        // Проверка существования пользователя
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }
        // Удаление пользователя и связанных данных
        ((UserRepository) userStorage).deleteUserById(userId);
    }



    public UserDto addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", friendId)));

        if (friendshipRepository.friendshipExists(userId, friendId)) {
            throw new ConditionsNotMetException(
                    String.format("Пользователь с ID - %d уже есть в списке друзей у пользователся с ID - %d",
                            friendId, userId));
        }

        friendshipRepository.addFriendship(userId, friendId);

        if (user.getFriends() == null) {
            user.setFriends(new ArrayList<>());
        }
        user.getFriends().add(friend);

        return UserMapper.mapToUserDto(user);
    }

    public List<UserDto> getFriends(Long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        List<Long> friends = friendshipRepository.findFriendsByUser(userId);

        return friends.stream()
                .map(friendId -> userStorage.getUserById(friendId).orElseThrow(() ->
                        new NotFoundException(String.format("Пользователь с ID - %d не найден", friendId))))
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", friendId)));

        if (!(friendshipRepository.friendshipExists(userId, friendId))) {
            return;
        }

        friendshipRepository.removeFriendship(userId, friendId);
    }

    public List<UserDto> getCommonFriend(Long userId, Long friendId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", friendId)));

        List<Long> commonFriends = friendshipRepository.findCommonFriends(userId, friendId);

        return commonFriends.stream()
                .map(commonFriend -> userStorage.getUserById(commonFriend).orElseThrow(() ->
                        new NotFoundException(String.format("Пользователь с ID - %d не найден", commonFriend))))
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getRecommendations(Long userId) {
        List<Long> userLikedFilms = filmStorage.getLikedFilmsByUser(userId);
        if (userLikedFilms.isEmpty()) return List.of();

        List<Long> similarUsers = filmStorage.getUsersLikedSameFilms(userLikedFilms, userId);
        if (similarUsers.isEmpty()) return List.of();

        Map<Long, Integer> userOverlap = new HashMap<>();

        for (Long otherUserId : similarUsers) {
            List<Long> otherLikedFilms = filmStorage.getLikedFilmsByUser(otherUserId);
            int overlap = (int) otherLikedFilms.stream()
                    .filter(userLikedFilms::contains)
                    .count();
            userOverlap.put(otherUserId, overlap);
        }

        Long bestMatchUser = userOverlap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (bestMatchUser == null) return List.of();

        List<Long> recommendedIds = filmStorage.getRecommendedFilmIds(userId, bestMatchUser);
        return recommendedIds.stream()
                .map(filmStorage::getFilmById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FilmMapper::mapToFilmDto)
                .collect(Collectors.toList());
    }

}
