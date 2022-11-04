package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestDtoWithItems;

import java.util.Collection;

public interface ItemRequestService {
    ItemRequestDto save(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestDtoWithItems> findAll(Long userId);

    Collection<ItemRequestDtoWithItems> findAllRequests(Long userId, int from, int size);

    ItemRequestDtoWithItems findByRequestId(Long userId, Long itemRequestId);
}
