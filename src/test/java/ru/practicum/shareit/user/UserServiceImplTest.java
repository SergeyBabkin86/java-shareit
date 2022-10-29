package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.user.mapper.UserMapper.toUserDto;

class UserServiceImplTest {

    private UserService userService;
    private UserRepository userRepository;
    private final List<User> users = new ArrayList<>();

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserServiceImpl(userRepository);

        users.add(new User(1L, "user1", "user1@mail.ru"));
        users.add(new User(2L, "user2", "user2@mail.ru"));
        users.add(new User(3L, "user3", "user3@mail.ru"));
    }

    @Test
    void saveTest() {
        var user = users.get(0);
        when(userRepository.save(any())).thenReturn(user);

        var expected = userService.save(toUserDto(user));

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(user.getId()));
        assertThat(expected.getName(), equalTo(user.getName()));
        assertThat(expected.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void updateTest() {
        var user1 = users.get(0);
        when(userRepository.existsById(user1.getId())).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user1));

        var user2 = users.get(1);
        user2.setId(user1.getId());

        var expected = userService.update(user1.getId(), toUserDto(user2));

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(user1.getId()));
        assertThat(expected.getName(), equalTo(user2.getName()));
        assertThat(expected.getEmail(), equalTo(user2.getEmail()));
    }

    @Test
    void deleteByIdTest() {
        var user = users.get(0);
        var userId = user.getId();

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteById(userId);

        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void updateWithNullFieldsTest() {
        User user = users.get(0);

        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        var expected = userService.update(1L, new UserDto(1L, null, null));

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(1L));
        assertThat(expected.getName(), equalTo(user.getName()));
        assertThat(expected.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void updateWithoutRepositoryTest() {
        User user = users.get(0);
        when(userRepository.save(any())).thenReturn(user);

        assertThrows(EntityNotFoundException.class, () -> userService.update(1L, toUserDto(user)));
    }

    @Test
    void getByIdTest() {
        User user = users.get(0);

        when(userRepository.existsById(user.getId())).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        var expected = userService.getById(1L);

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(1L));
        assertThat(expected.getName(), equalTo(user.getName()));
        assertThat(expected.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void getByIdWithoutRepositoryTest() {
        var userId = 1L;
        final EntityNotFoundException e = assertThrows(EntityNotFoundException.class,
                () -> userService.getById(userId));

        assertThat(e.getMessage(), equalTo(format("Пользователь с id: %s не найден.", userId)));
    }

    @Test
    void findAllTest() {
        when(userRepository.findAll()).thenReturn(users);

        var expected = new ArrayList<>(userService.findAll());

        assertNotNull(expected);
        assertEquals(3, expected.size());
        verify(userRepository, times(1)).findAll();
    }
}