package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/requests")
@Slf4j
@Validated
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto save(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @RequestBody ItemRequestDto itemRequestDto) {
        return itemRequestService.save(itemRequestDto, userId);
    }

    @GetMapping
    public Collection<ItemRequestDtoWithItems> findAll(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.findAll(userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDtoWithItems> findAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam(defaultValue = "0") int from,
                                                               @RequestParam(defaultValue = "20") int size) {
        return itemRequestService.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDtoWithItems findByRequestId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @PathVariable Long requestId) {
        return itemRequestService.findByRequestId(userId, requestId);
    }
}
