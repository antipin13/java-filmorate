package ru.yandex.practicum.filmorate.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.EventRepository;
import ru.yandex.practicum.filmorate.dal.dto.EventDto;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventService {
    final EventRepository eventRepository;
    final UserStorage userStorage;

    public EventService(EventRepository eventRepository, @Qualifier("dbStorage") UserStorage userStorage) {
        this.eventRepository = eventRepository;
        this.userStorage = userStorage;
    }

    public List<EventDto> getEventsByUserId(Long userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID - %d не найден", userId)));

        return eventRepository.findEventsByUser(userId)
                .stream()
                .map(EventMapper::mapToEventDto)
                .collect(Collectors.toList());
    }
}
