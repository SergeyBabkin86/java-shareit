package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long lastId = 1L;

    @Override
    public User add(User user) {
        user.setId(lastId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        checkAvailability(user.getId());
        users.replace(user.getId(), user);
        return user;
    }

    @Override
    public boolean delete(Long userId) {
        checkAvailability(userId);
        users.remove(userId);
        return !users.containsKey(userId);
    }

    @Override
    public User get(Long userId) {
        checkAvailability(userId);
        return users.get(userId);
    }

    @Override
    public Collection<User> getAll() {
        return new ArrayList<>(users.values());
    }

    private void checkAvailability(Long userId) {
        if (!users.containsKey(userId)) {
            log.debug("Пользователь с id: {} не найден.", userId);
            throw new EntityNotFoundException((String.format("Пользователь с id: %s не найден.", userId)));
        }
    }
}
