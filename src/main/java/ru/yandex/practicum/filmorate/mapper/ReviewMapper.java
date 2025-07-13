package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.model.Review;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewMapper {
    public static Review mapToReview(NewReviewRequest request) {
        Review review = Review.builder()
                .content(request.getContent())
                .isPositive(request.getIsPositive())
                .userId(request.getUserId())
                .filmId(request.getFilmId())
                .build();

        return review;
    }

    public static ReviewDto mapToReviewDto(Review review) {
        ReviewDto dto = ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.getIsPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();

        return dto;
    }

    public static Review updateReviewFields(Review review, UpdateReviewRequest request) {
        if (request.hasId()) {
            review.setReviewId(request.getReviewId());
        }
        if (request.hasContent()) {
            review.setContent(request.getContent());
        }
        if (request.hasIsPositive()) {
            review.setIsPositive(request.getIsPositive());
        }
        if (request.hasUserId()) {
            review.setUserId(request.getUserId());
        }
        if (request.hasFilmId()) {
            review.setFilmId(request.getFilmId());
        }
        if (request.hasUseful()) {
            review.setUseful(request.getUseful());
        }

        return review;
    }
}
