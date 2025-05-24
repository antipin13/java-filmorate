package ru.yandex.practicum.filmorate.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String parameter;
    private final String reason;

    public ValidationException(String parameter, String reason) {
        this.parameter = parameter;
        this.reason = reason;
    }
}
