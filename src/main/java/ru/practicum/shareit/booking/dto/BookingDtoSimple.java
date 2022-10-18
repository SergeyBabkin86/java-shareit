package ru.practicum.shareit.booking.dto;

import lombok.*;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoSimple {

    private Long id;

    @NotNull(message = "Не указано время начала бронирования.")
    @FutureOrPresent(message = "Время начала бронирования не может быть в прошлом.")
    private LocalDateTime start;

    @FutureOrPresent(message = "Время начала бронирования не может быть в прошлом.")
    @NotNull(message = "Не указано время окончания бронирования.")
    private LocalDateTime end;

    @NotNull(message = "Не указан id бронируемой вещи.")
    private Long itemId;
}
