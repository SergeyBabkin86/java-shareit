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
public class inMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private Long lastId = 0L;

    @Override
    public User addUser(User user) {
        user.setId(generateId());
        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    @Override
    public User updateUser(User user) {
        checkUserExists(user.getId());
        users.replace(user.getId(), user);
        return users.get(user.getId());
    }

    @Override
    public boolean deleteUser(Long userId) {
        checkUserExists(userId);
        users.remove(userId);
        return !users.containsKey(userId);
    }

    @Override
    public User getUser(Long userId) {
        checkUserExists(userId);
        return users.get(userId);
    }

    @Override
    public Collection<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    private Long generateId() {
        return ++lastId;
    }

    private void checkUserExists(Long userId) {
        if (!users.containsKey(userId)) {
            log.debug("Пользователь с id: {} не найден.", userId);
            throw new EntityNotFoundException((String.format("Пользователь с id: %s не найден.", userId)));
        }
    }
}
