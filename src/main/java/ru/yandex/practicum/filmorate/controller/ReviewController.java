package ru.yandex.practicum.filmorate.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.*;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewController {
    final ReviewService reviewService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReviewDto> getReviews(@RequestParam(required = false) Long filmId,
                                      @RequestParam(required = false, defaultValue = "10") Integer count) {
        if (filmId != null) {
            return reviewService.getReviewsByFilmId(filmId, count);
        } else {
            return reviewService.getReviews();
        }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto create(@RequestBody NewReviewRequest request) {
        log.info("Запрос на добавление отзыва {}", request);
        return reviewService.createReview(request);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<ReviewDto> findById(@PathVariable Long id) {
        log.info("Запрос на поиск и вывод отзыва с ID {}", id);
        return Optional.ofNullable(reviewService.getReviewById(id));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ReviewDto update(@RequestBody UpdateReviewRequest request) {
        return reviewService.updateReview(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public boolean delete(@PathVariable Long id) {
        return reviewService.deleteReview(id);
    }

    @PutMapping("/{id}/like/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id,
                        @PathVariable("user-id") Long userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void addDislike(@PathVariable Long id,
                           @PathVariable("user-id") Long userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeLike(@PathVariable Long id,
                           @PathVariable("user-id") Long userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeDislike(@PathVariable Long id,
                              @PathVariable("user-id") Long userId) {
        reviewService.removeDislike(id, userId);
    }
}
