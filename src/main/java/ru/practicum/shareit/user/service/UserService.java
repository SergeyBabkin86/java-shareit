package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    boolean deleteUser(Long userId);

    UserDto getUser(Long userId);

    Collection<UserDto> getUsers();
}
