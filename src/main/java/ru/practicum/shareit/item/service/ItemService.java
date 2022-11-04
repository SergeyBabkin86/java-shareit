package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;

import java.util.Collection;

public interface ItemService {
    ItemDto save(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto updItemDto);

    ItemDtoWithBooking findById(Long itemId, Long userId);

    Collection<ItemDtoWithBooking> findAll(Long userId, int from, int size);

    Collection<ItemDto> search(String searchRequest, int from, int size);

    void deleteById(Long itemId);

    CommentDto saveComment(Long userId, Long itemId, CommentDto commentDto);
}