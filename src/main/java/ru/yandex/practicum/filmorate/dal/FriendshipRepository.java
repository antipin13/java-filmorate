package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendshipRepository {
    final JdbcTemplate jdbc;

    static final String INSERT_QUERY = "INSERT INTO friendships(user_id, friend_id) VALUES (?, ?)";
    static final String CHECK_FRIENDSHIP = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
    static final String DELETE_FRIENDSHIP = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?)";
    static final String FIND_COMMON_FRIENDS = "SELECT f.friend_id FROM friendships f WHERE f.user_id = ? " +
            "AND EXISTS (SELECT 1 FROM friendships f2 WHERE f2.user_id = ? AND f2.friend_id = f.friend_id)";
    static final String FIND_FRIENDS_BY_USER = "SELECT friend_id FROM friendships WHERE user_id = ?";

    public FriendshipRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void addFriendship(Long userId, Long friendId) {
        jdbc.update(INSERT_QUERY, userId, friendId);
    }

    public boolean friendshipExists(Long userId, Long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIENDSHIP, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    public void removeFriendship(Long userId, Long friendId) {
        jdbc.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    public List<Long> findCommonFriends(Long userId1, Long userId2) {
        return jdbc.queryForList(FIND_COMMON_FRIENDS, Long.class, userId1, userId2);
    }

    public List<Long> findFriendsByUser(Long userId) {
        return jdbc.queryForList(FIND_FRIENDS_BY_USER, Long.class, userId);
    }
}
