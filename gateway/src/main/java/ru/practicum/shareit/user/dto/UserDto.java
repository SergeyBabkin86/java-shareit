
package ru.practicum.shareit.user.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Не указано имя пользователя.")
    private String name;

    @NotBlank(message = "Не указан адрес электронной почты.")
    @Email(message = "Некорректный формат адреса электронной почты.")
    private String email;
}