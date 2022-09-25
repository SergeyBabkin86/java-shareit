package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.util.Create;
import ru.practicum.shareit.util.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(groups = {Create.class}, message = "Не указано имя пользователя.")
    private String name;
    @NotBlank(groups = {Create.class}, message = "Не указан адрес электронной почты.")
    @Email(groups = {Create.class}, message = "Некорректный формат адреса электронной почты.")
    @Email(groups = {Update.class}, message = "Некорректный формат адреса электронной почты.")
    private String email;
}
