package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dal.dto.DirectorDto;
import ru.yandex.practicum.filmorate.dal.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dal.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.service.DirectorService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<DirectorDto> getDirectors() {
        log.info("Запрос на получения списка всех режиссеров");
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<DirectorDto> findById(@PathVariable Long id) {
        log.info("Запрос на получение режиссера c ID:{}", id);
        return Optional.ofNullable(directorService.getDirectorById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto createDirector(@RequestBody NewDirectorRequest request) {
        log.info("Запрос на добавление режиссера {}", request);
        return directorService.createDirector(request);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public DirectorDto updateDirector(@RequestBody UpdateDirectorRequest request) {
        log.info("Запрос на изменение режиссера {}", request);
        return directorService.updateDirector(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public String delete(@PathVariable Long id) {
        log.info("Запрос на удаление режиссера c ID:{}", id);
        if (directorService.deleteDirector(id)) {
            return String.format("Режиссер с ID:%s успешно удален", id);
        } else {
            return "Произошла ошибка во время удаления";
        }
    }
}
