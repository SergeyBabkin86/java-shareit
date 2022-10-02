package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemDto add(Long userId, ItemDto itemDto) {
        var item = itemRepository.add(mapToItem(itemDto, userRepository.get(userId)));
        return mapToItemDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto updItemDto) {
        checkItemOwner(userId, itemId);
        var itemDto = get(itemId);

        if (updItemDto.getName() != null) {
            itemDto.setName(updItemDto.getName());
        }

        if (updItemDto.getDescription() != null) {
            itemDto.setDescription(updItemDto.getDescription());
        }

        if (updItemDto.getAvailable() != null) {
            itemDto.setAvailable(updItemDto.getAvailable());
        }
        itemRepository.update(mapToItem(itemDto, userRepository.get(userId)));
        return itemDto;
    }

    @Override
    public ItemDto get(Long itemId) {
        return mapToItemDto(itemRepository.get(itemId));
    }

    @Override
    public Collection<ItemDto> getForUser(Long userId) {
        return itemRepository.getForUser(userId).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Collection<ItemDto> search(String searchRequest) {
        return itemRepository.search(searchRequest).stream()
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void checkItemOwner(Long userId, Long itemId) {
        if (!itemRepository.get(itemId).getOwner().getId().equals(userId)) {
            throw new RuntimeException((format("Вещь с id: %s не принадлежит пользователю с id: %s.", itemId, userId)));
        }
    }
}
