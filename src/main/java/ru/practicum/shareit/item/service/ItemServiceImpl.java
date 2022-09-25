package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItem;
import static ru.practicum.shareit.item.mapper.ItemMapper.mapToItemDto;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        var item = itemRepository.addItem(mapToItem(itemDto, userRepository.getUser(userId)));
        return mapToItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto updItemDto) {
        checkItemOwner(userId, itemId);
        var itemDto = getItem(itemId);

        if (updItemDto.getName() != null) {
            itemDto.setName(updItemDto.getName());
        }

        if (updItemDto.getDescription() != null) {
            itemDto.setDescription(updItemDto.getDescription());
        }

        if (updItemDto.getAvailable() != null) {
            itemDto.setAvailable(updItemDto.getAvailable());
        }
        itemRepository.updateItem(mapToItem(itemDto, userRepository.getUser(userId)));
        return itemDto;
    }

    @Override
    public ItemDto getItem(Long itemId) {
        return mapToItemDto(itemRepository.getItem(itemId));
    }

    @Override
    public Collection<ItemDto> getUsersItems(Long userId) {
        return itemRepository.getUsersItems(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<ItemDto> searchItems(String searchRequest) {
        return itemRepository.searchItems(searchRequest).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkItemOwner(Long userId, Long itemId) {
        if (!itemRepository.getItem(itemId).getOwner().getId().equals(userId)) {
            throw new RuntimeException((format("Вещь с id: %s не принадлежит пользователю с id: %s.", itemId, userId)));
        }
    }
}
