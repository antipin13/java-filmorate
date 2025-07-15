package ru.yandex.practicum.filmorate.dal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreDto implements Comparable<GenreDto> {
    @Override
    public int compareTo(GenreDto o) {
        return this.id.compareTo(o.id);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long id;
    String name;
}
