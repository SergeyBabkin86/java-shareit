package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto add(UserDto userDto) {
        checkEmailConflict(userDto.getEmail());
        var user = userRepository.add(mapToUser(userDto));
        return mapToUserDto(user);
    }

    @Override
    public UserDto update(Long userId, UserDto updUserDto) {
        var userDto = get(userId);

        if (updUserDto.getName() != null) {
            userDto.setName(updUserDto.getName());
        }
        if (updUserDto.getEmail() != null && !updUserDto.getEmail().equals(userDto.getEmail())) {
            checkEmailConflict(updUserDto.getEmail());
            userDto.setEmail(updUserDto.getEmail());
        }

        userRepository.update(mapToUser(userDto));
        return userDto;
    }

    @Override
    public boolean delete(Long userId) {
        return userRepository.delete(userId);
    }

    @Override
    public UserDto get(Long userId) {
        return mapToUserDto(userRepository.get(userId));
    }

    @Override
    public Collection<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkEmailConflict(String email) {
        getAll().stream().filter(user -> user.getEmail().equals(email)).forEach(user -> {
            throw new ConflictException((format("Email: %s уже используется.", email)));
        });
    }
}
