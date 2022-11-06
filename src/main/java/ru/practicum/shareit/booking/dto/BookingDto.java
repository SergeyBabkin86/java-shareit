package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private Long id;

    @NotNull(message = "Не указано время начала бронирования.")
    @FutureOrPresent(message = "Время начала бронирования не может быть в прошлом.")
    private LocalDateTime start;

    @FutureOrPresent(message = "Время начала бронирования не может быть в прошлом.")
    @NotNull(message = "Не указано время окончания бронирования.")
    private LocalDateTime end;

    private Item item;

    private User booker;

    private Status status;
}
