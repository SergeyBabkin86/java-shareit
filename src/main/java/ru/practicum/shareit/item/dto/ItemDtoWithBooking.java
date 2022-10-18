package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter @Setter
@EqualsAndHashCode
@AllArgsConstructor @NoArgsConstructor
public class ItemDtoWithBooking {

    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @NotNull
    private String name;

    @NotNull
    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    @NotNull
    private Boolean available;

    private BookingDtoForItem lastBooking;

    private BookingDtoForItem nextBooking;

    private List<CommentDto> comments;
}
