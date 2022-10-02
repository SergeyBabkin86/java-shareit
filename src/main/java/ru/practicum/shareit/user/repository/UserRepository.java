package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserRepository {

    User add(User user);

    User update(User user);

    boolean delete(Long userId);

    User get(Long userId);

    Collection<User> getAll();
}
