package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto save(UserDto userDto);

    UserDto update(Long userId, UserDto userDto);

    void deleteById(Long userId);

    UserDto getById(Long userId);

    Collection<UserDto> findAll();
}
