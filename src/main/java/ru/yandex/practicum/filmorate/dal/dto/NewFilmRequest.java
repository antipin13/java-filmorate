package ru.yandex.practicum.filmorate.dal.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import java.time.LocalDate;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewFilmRequest {
    String name;
    String description;
    LocalDate releaseDate;
    Integer duration;
    Rating mpa;
    Set<Genre> genres;
    Set<Director> directors;
}
