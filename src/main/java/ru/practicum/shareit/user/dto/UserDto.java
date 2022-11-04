package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.utilities.Create;
import ru.practicum.shareit.utilities.Update;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
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
