package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.utilities.Create;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank(groups = {Create.class}, message = "Не указано название вещи.")
    private String name;

    @NotBlank(groups = {Create.class}, message = "Не указано описание вещи.")
    private String description;

    @NotNull(groups = {Create.class}, message = "Ну указан статус доступности вещи.")
    private Boolean available;

    private Long requestId;
}
