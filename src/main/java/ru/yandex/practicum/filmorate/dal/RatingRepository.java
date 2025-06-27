package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingRepository extends BaseRepository<Rating> {
    static final String FIND_ALL_QUERY = "SELECT * FROM rating";
    static final String FIND_BY_ID_QUERY = "SELECT * FROM rating WHERE id = ?";

    public RatingRepository(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper, Rating.class);
    }

    public Optional<Rating> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Rating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }
}