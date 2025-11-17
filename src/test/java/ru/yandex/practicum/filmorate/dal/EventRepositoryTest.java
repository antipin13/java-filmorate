package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewRepository.class, ReviewRowMapper.class, FilmRepository.class, FilmRowMapper.class,
        RatingRepository.class, GenreRepository.class, RatingRowMapper.class, GenreRowMapper.class,
        UserRepository.class, UserRowMapper.class, DirectorRepository.class, DirectorRowMapper.class,
        EventRepository.class, EventRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class EventRepositoryTest {
    final JdbcTemplate jdbc;
    final ReviewRepository reviewRepository;
    final FilmRepository filmRepository;
    final UserRepository userRepository;
    final EventRepository eventRepository;
    Review review;
    User user;
    Film film;
    Rating mpa = new Rating();

    @BeforeEach
    void setUp() {
        mpa.setId(1L);
        mpa.setName("mpa");

        film = Film.builder()
                .name("film1")
                .description("description 1")
                .releaseDate(LocalDate.of(2007, 8, 13))
                .duration(120)
                .mpa(mpa)
                .build();

        user = User.builder()
                .id(null)
                .email("user@mail.ru")
                .login("login")
                .name("name")
                .birthday(LocalDate.of(1999, 06, 15))
                .build();

        Film createFilm = filmRepository.create(film);
        User createUser = userRepository.create(user);

        review = Review.builder()
                .content("content")
                .isPositive(false)
                .userId(createUser.getId())
                .filmId(createFilm.getId())
                .build();
    }

    @Test
    void testAddFeed() {
        Review createReview = reviewRepository.create(review);

        eventRepository.addEvent(Instant.now().toEpochMilli(), user.getId(), "REVIEW", "ADD",
                createReview.getReviewId());

        String feeds = "SELECT COUNT(*) FROM event";

        Integer count = jdbc.queryForObject(feeds, Integer.class);

        assertThat(count.equals(1));
    }

    @Test
    void testGetFeedsByUserId() {
        List<Event> feeds = eventRepository.findEventsByUser(user.getId());
        assertThat(feeds.size()).isEqualTo(0);

        Review createReview = reviewRepository.create(review);

        eventRepository.addEvent(Instant.now().toEpochMilli(), user.getId(), "REVIEW", "ADD",
                createReview.getReviewId());

        eventRepository.addEvent(Instant.now().toEpochMilli(), user.getId(), "LIKE", "ADD",
                createReview.getReviewId());

        feeds = eventRepository.findEventsByUser(user.getId());
        assertThat(feeds.size()).isEqualTo(2);
    }
}