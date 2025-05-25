package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage inMemoryUserStorage;

    @Autowired
    public UserService(UserStorage inMemoryUserStorage) {
        this.inMemoryUserStorage = inMemoryUserStorage;
    }

    public void addFriend(User user1, User user2) {
        log.info("Запрос на добавление друга {} в друзья к {}", user2, user1);

        user1.getFriends().add(user2.getId());
        user2.getFriends().add(user1.getId());
        log.info("Пользователь - {} успешно добавлен в друзья к {}", user2, user1);
    }

    public void deleteFriend(User user1, User user2) {
        user1.getFriends().remove(user2.getId());
        user2.getFriends().remove(user1.getId());
    }

    public List<User> getCommonFriends(User user1, User user2) {
        Set<Integer> commonFriends = new HashSet<>(user1.getFriends());

        commonFriends.retainAll(user2.getFriends());

        return commonFriends.stream()
                .map(inMemoryUserStorage::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getUserFriends(User user) {
        Set<Integer> userFriends = new HashSet<>(user.getFriends());

        return userFriends.stream()
                .map(inMemoryUserStorage::getUserById)
                .collect(Collectors.toList());
    }
}
