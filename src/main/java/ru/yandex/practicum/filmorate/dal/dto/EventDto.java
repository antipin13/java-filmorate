package ru.yandex.practicum.filmorate.dal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class EventDto {
    Long timestamp;
    Long userId;
    String eventType;
    String operation;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Long eventId;
    Long entityId;
}
