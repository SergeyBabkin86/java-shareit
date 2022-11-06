package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") long userId,
                                 @Valid @RequestBody ItemRequestDto itemRequestDto) {
        log.info("Метод POST /requests. UserId: {}", userId);
        return itemRequestClient.save(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Метод GET /requests. UserId: {}", userId);
        return itemRequestClient.findAll(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAllRequests(@RequestHeader("X-Sharer-User-Id") long userId,
                                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                         @Positive @RequestParam(defaultValue = "20") int size) {
        log.info("Метод GET /requests/all. UserId: {}, from: {}, size: {}", userId, from, size);
        return itemRequestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findByRequestId(@RequestHeader("X-Sharer-User-Id") long userId,
                                                   @PathVariable long requestId) {
        log.info("Метод GET /requests/all. UserId: {}, requestId: {}", userId, requestId);
        return itemRequestClient.findByRequestId(userId, requestId);
    }
}