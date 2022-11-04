package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserClient  userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("Метод Get /users");
        return userClient.getUsers();
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserDto userDto) {
        log.info("Метод Post /users, Пользователь: {}", userDto.getName());
        return userClient.save(userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findUserById(@PathVariable long id) {
        log.info("Метод GET с user {}", id);
        return userClient.getUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable long id) {
        log.info("Метод DELETE /users/id , userId: {}", id);
        userClient.deleteUser(id);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable long id, @RequestBody UserDto userDto) {
        log.info("Метод PATCH с user: {}", id);
        return userClient.updateUser(id, userDto);
    }
}