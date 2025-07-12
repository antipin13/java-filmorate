package ru.yandex.practicum.filmorate.dal.dto;

import lombok.Data;

@Data
public class UpdateDirectorRequest {
    Long id;
    String name;
    String lastName;
}
