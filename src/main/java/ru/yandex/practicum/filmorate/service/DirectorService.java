package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dal.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dal.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DirectorService {
    final DirectorRepository directorRepository;

    public DirectorDto getDirectorById(Long id) {
        return directorRepository.findDirectorById(id)
                .map(DirectorMapper::mapToDirectorDto)
                .orElseThrow(() -> new NotFoundException("Режиссер не найден с ID: " + id));
    }

    public List<DirectorDto> getDirectors() {
        return directorRepository.getDirectors().stream()
                .map(DirectorMapper::mapToDirectorDto)
                .collect(Collectors.toList());
    }

    public DirectorDto createDirector(NewDirectorRequest request) {
        Director director = DirectorMapper.mapToDirector(request);
        Director.validateDirector(director);
        director = directorRepository.create(director);
        return DirectorMapper.mapToDirectorDto(director);
    }

    public DirectorDto updateDirector(UpdateDirectorRequest request) {
        Long id = request.getId();

        if (directorRepository.findDirectorById(id).isEmpty()) {
            throw new NotFoundException(String.format("Режиссер с ID - %d не найден", id));
        }

        Director existingDirector = DirectorMapper.mapToDirector(request);
        Director.validateDirector(existingDirector);
        existingDirector =  directorRepository.update(existingDirector);
        return DirectorMapper.mapToDirectorDto(existingDirector);
    }

    public boolean deleteDirector(Long id) {
        Optional<Director> director = directorRepository.findDirectorById(id);
        if (director.isPresent()) {
            return directorRepository.delete(director.get());
        } else {
            throw new NotFoundException(String.format("Режиссер с ID - %d не найден", id));
        }
    }

    public Set<Director> getDirectorsByFilmId(Long filmId) {
        return new HashSet<>(directorRepository.findDirectorsByFilmId(filmId));
    }
}
