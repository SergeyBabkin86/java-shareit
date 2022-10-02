package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item add(Item item);

    Item update(Item item);

    Item get(Long itemId);

    Collection<Item> getForUser(Long userId);

    Collection<Item> search(String searchRequest);
}
