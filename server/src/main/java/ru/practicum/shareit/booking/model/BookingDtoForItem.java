package ru.practicum.shareit.booking.model;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoForItem {

    private Long id;

    private Long bookerId;
}
