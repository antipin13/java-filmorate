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
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({ReviewRepository.class, ReviewRowMapper.class, FilmRepository.class, FilmRowMapper.class,
        RatingRepository.class, GenreRepository.class, RatingRowMapper.class, GenreRowMapper.class,
        UserRepository.class, UserRowMapper.class, DirectorRepository.class, DirectorRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class ReviewRepositoryTest {
    final ReviewRepository reviewRepository;
    final FilmRepository filmRepository;
    final UserRepository userRepository;
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
    void testCreateReview() {
        Review createReview = reviewRepository.create(review);

        assertThat(createReview)
                .hasFieldOrPropertyWithValue("reviewId", createReview.getReviewId());
    }

    @Test
    public void testFindReviewById() {
        Review createReview = reviewRepository.create(review);

        Optional<Review> reviewOptional = reviewRepository.getReviewById(createReview.getReviewId());

        assertThat(reviewOptional)
                .isPresent()
                .hasValueSatisfying(review ->
                        assertThat(review).hasFieldOrPropertyWithValue("reviewId", createReview.getReviewId())
                );
    }

    @Test
    public void testUpdateReview() {
        Review createReview = reviewRepository.create(review);

        Review updateReview = Review.builder()
                .reviewId(createReview.getReviewId())
                .content("content")
                .isPositive(true)
                .userId(createReview.getUserId())
                .filmId(createReview.getFilmId())
                .build();

        Review updateReviewInDb = reviewRepository.update(updateReview);

        assertThat(updateReviewInDb)
                .hasFieldOrPropertyWithValue("isPositive", true);
    }

    @Test
    public void testDeleteReview() {
        Review createReview = reviewRepository.create(review);

        boolean delete = reviewRepository.delete(createReview);

        assertThat(delete).isEqualTo(true);
    }

    @Test
    public void testGetReviews() {
        Review createReview = reviewRepository.create(review);

        Review review2 = Review.builder()
                .content("content")
                .isPositive(false)
                .userId(createReview.getUserId())
                .filmId(createReview.getFilmId())
                .build();

        reviewRepository.create(review2);

        List<Review> reviews = reviewRepository.getReviews();

        assertThat(reviews.size()).isEqualTo(2);
    }

    @Test
    public void testGetReviewByFilmId() {
        Review createReview = reviewRepository.create(review);

        Review review2 = Review.builder()
                .content("content")
                .isPositive(false)
                .userId(createReview.getUserId())
                .filmId(createReview.getFilmId())
                .build();

        reviewRepository.create(review2);

        List<Review> reviewsOfFilm = reviewRepository.getReviewByFilmId(createReview.getFilmId(), 10);

        assertThat(reviewsOfFilm.size()).isEqualTo(2);
    }

    @Test
    public void testAddRemoveLike() {
        Review createReview = reviewRepository.create(review);

        reviewRepository.addLike(createReview.getReviewId());

        Optional<Review> updatedReview = reviewRepository.getReviewById(createReview.getReviewId());

        assertThat(updatedReview.get())
                .hasFieldOrPropertyWithValue("useful", 1);

        reviewRepository.removeLike(createReview.getReviewId());

        updatedReview = reviewRepository.getReviewById(createReview.getReviewId());

        assertThat(updatedReview.get())
                .hasFieldOrPropertyWithValue("useful", 0);
    }

    @Test
    public void testAddRemoveDisike() {
        Review createReview = reviewRepository.create(review);

        reviewRepository.addDislike(createReview.getReviewId());

        Optional<Review> updatedReview = reviewRepository.getReviewById(createReview.getReviewId());

        assertThat(updatedReview.get())
                .hasFieldOrPropertyWithValue("useful", -1);

        reviewRepository.removeDislike(createReview.getReviewId());

        updatedReview = reviewRepository.getReviewById(createReview.getReviewId());

        assertThat(updatedReview.get())
                .hasFieldOrPropertyWithValue("useful", 0);
    }
}