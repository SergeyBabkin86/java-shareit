package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        var itemDto = new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null);

        var request = item.getItemRequest();

        if (request != null) {
            itemDto.setRequestId(request.getId());
        }
        return itemDto;
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                null,
                null);
    }

    public static ItemDtoWithBooking toItemDtoWithBooking(Item item) {
        return new ItemDtoWithBooking(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                null,
                null,
                new ArrayList<>());
    }
}
