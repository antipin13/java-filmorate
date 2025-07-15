package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRepository extends BaseRepository<Review> {
    static final String INSERT_QUERY = "INSERT INTO review(content, is_positive, user_id, film_id)" +
            "VALUES (?, ?, ?, ?)";
    static final String UPDATE_QUERY = "UPDATE review SET content = ?, is_positive = ?, user_id = ?, film_id = ?," +
            "useful = ? WHERE id = ?";
    static final String DELETE_QUERY = "DELETE FROM review WHERE id = ?";
    static final String FIND_BY_ID_QUERY = "SELECT * FROM review WHERE id = ?";
    static final String FIND_ALL_QUERY = "SELECT * FROM review ORDER BY useful DESC";
    static final String FIND_BY_ID_FILM_ALL = "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC";
    static final String FIND_BY_ID_FILM_LIMIT = "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
    static final String ADD_LIKE = "UPDATE review SET useful = useful + 1 WHERE id = ?";
    static final String ADD_DISLIKE = "UPDATE review SET useful = useful - 1 WHERE id = ?";
    static final String REMOVE_LIKE = "UPDATE review SET useful = useful - 1 WHERE id = ?";
    static final String REMOVE_DISLIKE = "UPDATE review SET useful = useful + 1 WHERE id = ?";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper, Review.class);
    }

    public Review create(Review review) {
        Long id = insert(
                INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId()
        );
        review.setReviewId(id);
        review.setUseful(0);

        return review;
    }

    public Review update(Review review) {
        update(
                UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getUseful(),
                review.getReviewId()
        );
        return review;
    }

    public boolean delete(Review review) {
        return delete(DELETE_QUERY, review.getReviewId());
    }

    public List<Review> getReviews() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Review> getReviewById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public List<Review> getReviewByFilmId(Long filmId, Integer countReview) {
        if (filmId == null) {
            return getReviews();
        }

        if (countReview == null) {
            return findMany(FIND_BY_ID_FILM_ALL, filmId);
        }

        return findMany(FIND_BY_ID_FILM_LIMIT, filmId, countReview);
    }

    public void addLike(Long reviewId) {
        jdbc.update(ADD_LIKE, reviewId);
    }

    public void addDislike(Long reviewId) {
        jdbc.update(ADD_DISLIKE, reviewId);
    }

    public void removeLike(Long reviewId) {
        jdbc.update(REMOVE_LIKE, reviewId);
    }

    public void removeDislike(Long reviewId) {
        jdbc.update(REMOVE_DISLIKE, reviewId);
    }
}
