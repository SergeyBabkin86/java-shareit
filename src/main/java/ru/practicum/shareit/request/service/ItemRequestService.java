package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoWithItems;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto save(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestDtoWithItems> findAll(Long userId);

    Collection<ItemRequestDtoWithItems> findAllRequests(Long userId, int from, int size);

    ItemRequestDtoWithItems findByRequestId(Long userId, Long itemRequestId);
}
