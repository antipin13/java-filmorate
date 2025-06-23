package ru.yandex.practicum.filmorate.dal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.time.LocalDate;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Rating mpa;
    List<Genre> genres;
}
