package ru.yandex.practicum.filmorate.dal.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateFilmRequest {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Rating mpa;
    List<Genre> genres;

    public boolean hasId() {
        return !(id == null);
    }

    public boolean hasName() {
        return !(name == null || name.isBlank());
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasReleaseDate() {
        return !(releaseDate == null);
    }

    public boolean hasDuration() {
        return !(duration == null);
    }

    public boolean hasMpa() {
        return !(mpa == null);
    }

    public boolean hasGenres() {
        return !(genres == null || genres.isEmpty());
    }
}
