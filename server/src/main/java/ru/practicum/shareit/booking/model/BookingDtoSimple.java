package ru.practicum.shareit.booking.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoSimple {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;
}
