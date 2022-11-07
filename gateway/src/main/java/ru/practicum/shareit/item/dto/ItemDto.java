package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotNull
    @NotBlank(message = "Не указано название вещи.")
    private String name;

    @NotNull
    @NotBlank(message = "Не указано название вещи.")
    private String description;

    @NotNull(message = "Ну указан статус доступности вещи.")
    private Boolean available;

    private Long requestId;
}