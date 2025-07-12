package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
@Qualifier("dbStorage")
public class UserRepository extends BaseRepository<User> implements UserStorage {
    static final String INSERT_QUERY = "INSERT INTO users(email, login, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    static final String DELETE_QUERY = "DELETE FROM users WHERE id = ?";
    static final String FIND_ALL_QUERY = "SELECT * FROM users";

    @Autowired
    public UserRepository(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper, User.class);
    }

    @Override
    public User create(User user) {
        Long id = insert(
                INSERT_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday())
        );
        user.setId(id);

        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public boolean delete(User user) {
        return delete(DELETE_QUERY, user.getId());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public List<User> getUsers() {
        return findMany(FIND_ALL_QUERY);
    }
}
