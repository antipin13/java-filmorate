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
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserRepository.class, UserRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class UserRepositoryTest {
    final UserRepository userRepository;
    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(null)
                .email("user@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1999,06,15))
                .build();
    }

    @AfterEach
    void afterEach() {
        user = User.builder()
                .id(null)
                .email("user@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1999,06,15))
                .build();
    }

    @Test
    public void testCreateUser() {
        User createUser = userRepository.create(user);

        assertThat(createUser)
                .hasFieldOrPropertyWithValue("id", Long.valueOf(2));
    }

    @Test
    public void testFindUserById() {
        userRepository.create(user);

        Optional<User> userOptional = userRepository.getUserById(Long.valueOf(3));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user1 ->
                        assertThat(user1).hasFieldOrPropertyWithValue("id", Long.valueOf(3))
                );
    }

    @Test
    public void updateUser() {
        userRepository.create(user);

        User updateUser = User.builder()
                .id(1L)
                .email("updateUser@mail.ru")
                .login("updateLogin")
                .name("updateName")
                .birthday(LocalDate.of(2000,06,15))
                .build();

        User updateUserInDb = userRepository.update(updateUser);

        assertThat(updateUserInDb)
                .hasFieldOrPropertyWithValue("email", "updateUser@mail.ru");
    }

    @Test
    public void deleteUser() {
        User deleteUser = userRepository.create(user);

        boolean delete = userRepository.delete(deleteUser);

        assertThat(delete).isEqualTo(true);
    }

    @Test
    public void getUsers() {
        userRepository.create(user);

        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("login2")
                .name("name2")
                .birthday(LocalDate.of(1999,06,15))
                .build();

        userRepository.create(user2);

        List<User> users = userRepository.getUsers();

        assertThat(users.size()).isEqualTo(2);
    }
}