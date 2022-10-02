package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;

public interface ItemService {
    ItemDto add(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto updItemDto);

    ItemDto get(Long itemId);

    Collection<ItemDto> getForUser(Long userId);

    Collection<ItemDto> search(String searchRequest);
}
