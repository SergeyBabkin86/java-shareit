package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@Slf4j
@Validated
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                          @Positive @RequestParam(defaultValue = "20") int size) {
        log.info("Метод Get. Поиск всех вещей пользователя с ID: {}", userId);
        return itemClient.getAll(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemDto itemDto,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Получен Post запрос /items. item: {}, наименование: {}", itemDto.getName(),
                itemDto.getDescription());
        return itemClient.save(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
                                         @PathVariable long id,
                                         @RequestBody ItemDto itemDto) {
        log.info("Метод PATCH user id={}, item id={}", userId, id);
        return itemClient.update(userId, id, itemDto);
    }


    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                                @Valid @RequestBody CommentDto commentDto,
                                                @PathVariable long itemId) {
        log.info("Метод Post: /items/{itemId}/comment. Вещь: {}, Комментарий: {}", itemId,
                commentDto.getText());
        return itemClient.saveComment(userId, itemId, commentDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> findItemById(@PathVariable long itemId, @RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Метод Get item: {}", itemId);
        return itemClient.getItem(itemId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteItemById(@PathVariable long id) {
        log.info("Метод Delete item: {}", id);
        itemClient.deleteById(id);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> findItemByText(@RequestParam String text,
                                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                 @Positive @RequestParam(defaultValue = "20") int size) {
        log.info("Метод Get /search: {}", text);
        return itemClient.searchItem(text, from, size);
    }
}