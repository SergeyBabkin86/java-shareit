package ru.practicum.shareit.request.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {

    private Long id;

    @NotNull
    @NotBlank(message = "Описание запроса не может быть пустым.")
    private String description;

    private LocalDateTime created;
}
