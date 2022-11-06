package ru.practicum.shareit.item.model;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingDtoForItem;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoWithBooking {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private BookingDtoForItem lastBooking;

    private BookingDtoForItem nextBooking;

    private List<CommentDto> comments;
}
