package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.BookingTransactionException;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDtoForItem;
import static ru.practicum.shareit.item.mapper.CommentMapper.mapToComment;
import static ru.practicum.shareit.item.mapper.CommentMapper.mapToCommentDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.utilities.Checker.checkItemAvailability;
import static ru.practicum.shareit.utilities.Checker.checkUserAvailability;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto save(Long userId, ItemDto itemDto) {
        checkUserAvailability(userId, userRepository);
        return mapToItemDto(itemRepository.save(mapToItem(itemDto, userRepository.findById(userId).get())));
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        checkUserAvailability(userId, userRepository);
        checkItemAvailability(itemId, itemRepository);
        checkItemOwner(userId, itemId);

        var oldItem = itemRepository.findById(itemId).get();

        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        return mapToItemDto(itemRepository.save(oldItem));
    }

    @Override
    public void deleteById(Long itemId) {
        checkItemAvailability(itemId, itemRepository);
        itemRepository.deleteById(itemId);
    }

    @Override
    public ItemDtoWithBooking findById(Long itemId, Long userId) {
        checkItemAvailability(itemId, itemRepository);
        checkUserAvailability(userId, userRepository);

        var item = itemRepository.findById(itemId).get();
        var itemDtoWithBooking = mapToItemDtoWithBooking(item);
        var comments = commentRepository.findAllByItemId(itemId);

        if (item.getOwner().getId().equals(userId)) {
            createItemDtoWithBooking(itemDtoWithBooking);
        }

        if (!comments.isEmpty()) {
            itemDtoWithBooking.setComments(comments
                    .stream().map(CommentMapper::mapToCommentDto)
                    .collect(Collectors.toList()));
        }
        return itemDtoWithBooking;
    }

    @Override
    public Collection<ItemDtoWithBooking> findAllForUser(Long userId) {
        checkUserAvailability(userId, userRepository);

        var result = itemRepository.findAll().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::mapToItemDtoWithBooking)
                .collect(Collectors.toList());

        for (ItemDtoWithBooking itemDtoWithBooking : result) {
            createItemDtoWithBooking(itemDtoWithBooking);

            var comments = commentRepository.findAllByItemId(itemDtoWithBooking.getId());

            if (!comments.isEmpty()) {
                itemDtoWithBooking.setComments(comments.stream()
                        .map(CommentMapper::mapToCommentDto)
                        .collect(Collectors.toList()));
            }
        }
        result.sort(Comparator.comparing(ItemDtoWithBooking::getId));
        return result;
    }

    @Override
    public Collection<ItemDto> search(String text) {
        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::mapToItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto saveComment(Long userId, Long itemId, CommentDto commentDto) {
        checkUserAvailability(userId, userRepository);
        checkItemAvailability(itemId, itemRepository);

        if (bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBefore(userId, itemId, LocalDateTime.now())
                .stream()
                .noneMatch(booking -> booking.getStatus().equals(Status.APPROVED))) {
            throw new BookingTransactionException(format("Пользователь с id: %s не брал в аренду вещь с id: %s.",
                    userId,
                    itemId));
        }

        var comment = mapToComment(commentDto);
        comment.setItem(itemRepository.findById(itemId).get());
        comment.setAuthor(userRepository.findById(userId).get());

        return mapToCommentDto(commentRepository.save(comment));
    }

    private void checkItemOwner(Long userId, Long itemId) {
        if (!itemRepository.findById(itemId).get().getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException((format("Вещь с id: %s не принадлежит пользователю с id: %s.",
                    itemId,
                    userId)));
        }
    }

    private void createItemDtoWithBooking(ItemDtoWithBooking itemDtoWithBooking) {
        var lastBookings = bookingRepository
                .findBookingsByItemIdAndEndIsBeforeOrderByEndDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now());

        if (!lastBookings.isEmpty()) {
            var lastBooking = toBookingDtoForItem(lastBookings.get(0));
            itemDtoWithBooking.setLastBooking(lastBooking);
        }

        var nextBookings = bookingRepository
                .findBookingsByItemIdAndStartIsAfterOrderByStartDesc(itemDtoWithBooking.getId(),
                        LocalDateTime.now());

        if (!nextBookings.isEmpty()) {
            var nextBooking = toBookingDtoForItem(nextBookings.get(0));
            itemDtoWithBooking.setNextBooking(nextBooking);
        }
    }
}
