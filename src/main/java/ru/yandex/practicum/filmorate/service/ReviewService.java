package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewService {
    final ReviewRepository reviewRepository;
    final FilmStorage filmStorage;
    final UserStorage userStorage;
    final EventRepository eventRepository;

    public ReviewService(ReviewRepository reviewRepository, @Qualifier("dbStorage") FilmStorage filmStorage,
                         @Qualifier("dbStorage") UserStorage userStorage, EventRepository eventRepository) {
        this.reviewRepository = reviewRepository;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.eventRepository = eventRepository;
    }

    public ReviewDto createReview(NewReviewRequest request) {
        Review review = ReviewMapper.mapToReview(request);

        Review.validateReview(review);

        Long filmId = review.getFilmId();

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм не найден с ID: " + filmId));

        Long userId = review.getUserId();

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));

        review = reviewRepository.create(review);
        log.info("Добавлен отзыв в БД {}", review);

        eventRepository.addEvent(Instant.now().toEpochMilli(), userId, EventType.REVIEW.toString(),
                Operation.ADD.toString(), review.getReviewId());

        return ReviewMapper.mapToReviewDto(review);
    }

    public ReviewDto getReviewById(Long reviewId) {
        return reviewRepository.getReviewById(reviewId)
                .map(ReviewMapper::mapToReviewDto)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с ID - %d не найден", reviewId)));
    }

    public List<ReviewDto> getReviews() {
        return reviewRepository.getReviews()
                .stream()
                .map(ReviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsByFilmId(Long filmId, Integer countReviews) {
        return reviewRepository.getReviewByFilmId(filmId, countReviews)
                .stream()
                .map(ReviewMapper::mapToReviewDto)
                .collect(Collectors.toList());
    }

    public ReviewDto updateReview(UpdateReviewRequest request) {
        Long reviewId = request.getReviewId();

        Review existingReview = reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException(String.format("Отзыв с ID - %d не найден", reviewId)));

        existingReview = ReviewMapper.updateReviewFields(existingReview, request);

        existingReview = reviewRepository.update(existingReview);

        eventRepository.addEvent(Instant.now().toEpochMilli(), existingReview.getUserId(), EventType.REVIEW.toString(),
                Operation.UPDATE.toString(), existingReview.getReviewId());

        return ReviewMapper.mapToReviewDto(existingReview);
    }

    public boolean deleteReview(Long id) {
        Optional<Review> reviewOpt = reviewRepository.getReviewById(id);
        if (reviewOpt.isPresent()) {
            eventRepository.addEvent(Instant.now().toEpochMilli(), reviewOpt.get().getUserId(),
                    EventType.REVIEW.toString(), Operation.REMOVE.toString(), reviewOpt.get().getReviewId());

            return reviewRepository.delete(reviewOpt.get());
        } else {
            throw new NotFoundException(String.format("Отзыв с ID - %d не найден", id));
        }
    }

    public void addLike(Long reviewId, Long userId) {
        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден с ID: " + reviewId));

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));

        reviewRepository.addLike(reviewId);

        eventRepository.addEvent(Instant.now().toEpochMilli(), userId, EventType.LIKE.toString(),
                Operation.ADD.toString(), reviewId);
    }

    public void addDislike(Long reviewId, Long userId) {
        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден с ID: " + reviewId));

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));

        if (eventRepository.existsLikeOnReviewByUser(userId, reviewId)) {
            reviewRepository.removeLike(reviewId);
            reviewRepository.addDislike(reviewId);
        } else {
            reviewRepository.addDislike(reviewId);
        }
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден с ID: " + reviewId));

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));

        reviewRepository.removeLike(reviewId);

        eventRepository.addEvent(Instant.now().toEpochMilli(), userId, EventType.LIKE.toString(),
                Operation.REMOVE.toString(), reviewId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewRepository.getReviewById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден с ID: " + reviewId));

        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден с ID: " + userId));

        reviewRepository.removeDislike(reviewId);
    }
}
