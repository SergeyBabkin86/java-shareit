package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto add(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    boolean delete(Long userId);

    UserDto get(Long userId);

    Collection<UserDto> getAll();
}
