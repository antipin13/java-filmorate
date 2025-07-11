package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.RatingRepository;
import ru.yandex.practicum.filmorate.dal.dto.RatingDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.RatingMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RatingService {
    final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    public RatingDto getRatingById(Long mpaId) {
        return ratingRepository.findById(mpaId)
                .map(RatingMapper::mapToRatingDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг не найден с ID: " + mpaId));
    }

    public List<RatingDto> getRatings() {
        return ratingRepository.findAll()
                .stream()
                .map(RatingMapper::mapToRatingDto)
                .collect(Collectors.toList());
    }
}
