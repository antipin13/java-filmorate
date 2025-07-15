package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreRepository extends BaseRepository<Genre> {
    static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ? ORDER BY id ASC";
    static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY id ASC";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Set<Genre> findAll() {
        return new HashSet<>(findMany(FIND_ALL_QUERY));
    }

    public Set<Genre> findGenresByFilmId(Long filmId) {
        String searchGenresSql = """
                SELECT g.*
                FROM genre g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id ASC
                """;
        return new HashSet<>(findMany(searchGenresSql, filmId));
    }
}