package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRepository extends BaseRepository<Event> {
    static final String INSERT_QUERY = "INSERT INTO event(timestamp, user_id, eventType, operation, entity_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    static final String FIND_EVENTS_BY_USER = "SELECT * FROM event WHERE user_id = ?";

    public EventRepository(JdbcTemplate jdbc, RowMapper<Event> mapper) {
        super(jdbc, mapper, Event.class);
    }

    public void addEvent(Long timestamp, Long userId, String eventType, String operation, Long entity_id) {
        jdbc.update(INSERT_QUERY, timestamp, userId, eventType, operation, entity_id);
    }

    public List<Event> findEventsByUser(Long userId) {
        return findMany(FIND_EVENTS_BY_USER, userId);
    }
}
