package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipRepository.class, UserRepository.class, UserRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class FriendshipRepositoryTest {
    final JdbcTemplate jdbc;
    final FriendshipRepository friendshipRepository;
    final UserRepository userRepository;
    User user1;
    User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(null)
                .email("user1@mail.ru")
                .login("login1")
                .name("name1")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();

        user2 = User.builder()
                .id(null)
                .email("user2@mail.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();
    }

    @AfterEach
    void afterEach() {
        user1 = User.builder()
                .id(null)
                .email("user1@mail.ru")
                .login("login1")
                .name("name1")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();

        user2 = User.builder()
                .id(null)
                .email("user2@mail.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();
    }

    @Test
    void createFriendship() {
        User createUser1 = userRepository.create(user1);
        User createUser2 = userRepository.create(user2);

        friendshipRepository.addFriendship(createUser1.getId(), createUser2.getId());

        String countFriendships = "SELECT COUNT(*) FROM friendships";

        Integer count = jdbc.queryForObject(countFriendships, Integer.class);

        assertThat(count.equals(1));
    }

    @Test
    void friendshipExist() {
        User createUser1 = userRepository.create(user1);
        User createUser2 = userRepository.create(user2);

        friendshipRepository.addFriendship(createUser1.getId(), createUser2.getId());

        boolean friendshipExist = friendshipRepository.friendshipExists(createUser1.getId(), createUser2.getId());

        assertThat(friendshipExist).isEqualTo(true);
    }

    @Test
    void removeFriendship() {
        User createUser1 = userRepository.create(user1);
        User createUser2 = userRepository.create(user2);

        friendshipRepository.addFriendship(createUser1.getId(), createUser2.getId());
        friendshipRepository.removeFriendship(createUser1.getId(), createUser2.getId());

        String countFriendships = "SELECT COUNT(*) FROM friendships";

        Integer count = jdbc.queryForObject(countFriendships, Integer.class);

        assertThat(count.equals(0));
    }

    @Test
    void getFriendsByUser() {
        User createUser1 = userRepository.create(user1);
        User createUser2 = userRepository.create(user2);

        friendshipRepository.addFriendship(createUser1.getId(), createUser2.getId());

        List<Long> friends = friendshipRepository.findFriendsByUser(createUser1.getId());

        assertThat(friends.size()).isEqualTo(1);
    }

    @Test
    void getCommonFriends() {
        User user3 = User.builder()
                .email("user3@mail.ru")
                .login("login3")
                .name("name3")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();

        User createUser1 = userRepository.create(user1);
        User createUser2 = userRepository.create(user2);
        User createUser3 = userRepository.create(user3);

        friendshipRepository.addFriendship(createUser1.getId(), createUser2.getId());
        friendshipRepository.addFriendship(createUser3.getId(), createUser2.getId());

        List<Long> commonFriends = friendshipRepository.findCommonFriends(createUser1.getId(), createUser3.getId());

        assertThat(commonFriends.get(0)).isEqualTo(createUser2.getId());
    }
}