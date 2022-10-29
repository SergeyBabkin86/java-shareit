package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.EmailValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.user.mapper.UserMapper.toUser;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;
import static ru.practicum.shareit.utilities.Checker.checkUserAvailability;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto save(UserDto userDto) {
        return toUserDto(userRepository.save(toUser(userDto)));
    }

    @Override
    public UserDto update(Long userId, UserDto userDto) {
        var oldUserDto = getById(userId);

        if (userDto.getName() != null) {
            oldUserDto.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equals(oldUserDto.getEmail())) {
            checkEmailConflict(userDto.getEmail());
            oldUserDto.setEmail(userDto.getEmail());
        }

        userRepository.save(toUser(oldUserDto));
        return oldUserDto;
    }

    @Override
    public void deleteById(Long userId) {
        checkUserAvailability(userId, userRepository);
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto getById(Long userId) {
        checkUserAvailability(userId, userRepository);
        return toUserDto(userRepository.findById(userId).get());
    }

    @Override
    public Collection<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkEmailConflict(String email) {
        findAll().stream().filter(user -> user.getEmail().equals(email)).forEach(user -> {
            throw new EmailValidationException((format("Email: %s уже используется.", email)));
        });
    }
}
