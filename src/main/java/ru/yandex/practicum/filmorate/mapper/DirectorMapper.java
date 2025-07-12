package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dal.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dal.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dal.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DirectorMapper {
    public static Director mapToDirector(NewDirectorRequest request) {
        Director director = new Director();
        director.setFirstName(request.getName());
        director.setLastName(request.getLastName());
        return director;
    }

    public static Director mapToDirector(UpdateDirectorRequest request) {
        Director director = new Director();
        director.setId(request.getId());
        director.setFirstName(request.getName());
        director.setLastName(request.getLastName());
        return director;
    }

    public static DirectorDto mapToDirectorDto(Director director) {
        DirectorDto directorDto = new DirectorDto();
        directorDto.setId(director.getId());
        directorDto.setName(director.getFirstName());
        directorDto.setLastName(director.getLastName());
        return directorDto;
    }
}
