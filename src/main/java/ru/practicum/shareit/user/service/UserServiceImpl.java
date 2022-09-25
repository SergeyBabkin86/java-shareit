package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUser;
import static ru.practicum.shareit.user.mapper.UserMapper.mapToUserDto;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        checkEmailConflict(userDto.getEmail());
        var user = userRepository.addUser(mapToUser(userDto));
        return mapToUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto updUserDto) {
        var userDto = getUser(userId);

        if (updUserDto.getName() != null) {
            userDto.setName(updUserDto.getName());
        }
        if (updUserDto.getEmail() != null && !updUserDto.getEmail().equals(userDto.getEmail())) {
            checkEmailConflict(updUserDto.getEmail());
            userDto.setEmail(updUserDto.getEmail());
        }

        userRepository.updateUser(mapToUser(userDto));
        return userDto;
    }

    @Override
    public boolean deleteUser(Long userId) {
        return userRepository.deleteUser(userId);
    }

    @Override
    public UserDto getUser(Long userId) {
        return mapToUserDto(userRepository.getUser(userId));
    }

    @Override
    public Collection<UserDto> getUsers() {
        return userRepository.getUsers().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkEmailConflict(String email) {
        getUsers().stream().filter(user -> user.getEmail().equals(email)).forEach(user -> {
            throw new ConflictException((format("Email: %s уже используется.", email)));
        });
    }
}
