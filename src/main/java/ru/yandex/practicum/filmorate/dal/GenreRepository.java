package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreRepository extends BaseRepository<Genre> {
    static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE id = ?";
    static final String FIND_ALL_QUERY = "SELECT * FROM genre";

    public GenreRepository(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper, Genre.class);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public List<Genre> findGenresByFilmId(Long filmId) {
        String searchGenresSql = "SELECT g.* FROM genre g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return findMany(searchGenresSql, filmId);
    }
}