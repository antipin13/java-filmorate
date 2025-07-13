package ru.yandex.practicum.filmorate.dal.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateReviewRequest {
    Long reviewId;
    String content;
    Boolean isPositive;
    Long userId;
    Long filmId;
    Integer useful;

    public boolean hasId() {
        return !(reviewId == null);
    }

    public boolean hasContent() {
        return !(content == null || content.isBlank());
    }

    public boolean hasIsPositive() {
        return !(isPositive == null);
    }

    public boolean hasUserId() {
        return !(userId == null);
    }

    public boolean hasFilmId() {
        return !(filmId == null);
    }

    public boolean hasUseful() {
        return !(useful == null);
    }
}
