package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private Long lastId = 1L;

    @Override
    public Item add(Item item) {
        item.setId(lastId++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        checkAvailability(item.getId());
        items.replace(item.getId(), item);
        return item;
    }

    @Override
    public Item get(Long itemId) {
        checkAvailability(itemId);
        return items.get(itemId);
    }

    @Override
    public Collection<Item> getForUser(Long userId) {
        return items.values().stream()
                .filter((item) -> item.getOwner().getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Item> search(String searchRequest) {
        if (searchRequest.isEmpty()) {
            return new ArrayList<>();
        }
        return items.values().stream()
                .filter(item1 -> item1.isAvailable()
                        && (item1.getName().toLowerCase().contains(searchRequest.toLowerCase())
                        || item1.getDescription().toLowerCase().contains(searchRequest.toLowerCase())))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkAvailability(Long itemId) {
        if (!items.containsKey(itemId)) {
            throw new EntityNotFoundException((String.format("Вещь с id: %s не найдена.", itemId)));
        }
    }
}
