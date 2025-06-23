package ru.yandex.practicum.filmorate.dal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({RatingRepository.class, RatingRowMapper.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class RatingRepositoryTest {
    final RatingRepository ratingRepository;

    @Test
    void getRatingById() {
        Optional<Rating> mpa = ratingRepository.findById(1L);

        assertThat(mpa)
                .isPresent()
                .hasValueSatisfying(rating ->
                        assertThat(rating).hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    void getAllRatings() {
        List<Rating> ratings = ratingRepository.findAll();

        assertThat(ratings.size()).isEqualTo(5);
    }
}