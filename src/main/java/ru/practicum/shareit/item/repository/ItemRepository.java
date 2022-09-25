package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item addItem(Item item);

    Item updateItem(Item item);

    Item getItem(Long itemId);

    Collection<Item> getUsersItems(Long userId);

    Collection<Item> searchItems(String searchRequest);
}
