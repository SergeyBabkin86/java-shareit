package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookingTransactionException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.CommentDto;
import ru.practicum.shareit.item.model.ItemDto;
import ru.practicum.shareit.item.model.ItemDtoWithBooking;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDtoForItem;
import static ru.practicum.shareit.item.mapper.CommentMapper.toComment;
import static ru.practicum.shareit.item.mapper.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.*;
import static ru.practicum.shareit.utilities.Checker.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto save(Long userId, ItemDto itemDto) {
        checkUserAvailability(userId, userRepository);

        var item = toItem(itemDto);
        item.setOwner(userRepository.findById(userId).get());

        var requestId = itemDto.getRequestId();
        if (requestId != null) {
            checkRequestAvailability(requestId, itemRequestRepository);
            item.setItemRequest(itemRequestRepository.findById(requestId).get());
        }
        return toItemDto(itemRepository.save(item));
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
        return toItemDto(itemRepository.save(oldItem));
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
        var itemDtoWithBooking = toItemDtoWithBooking(item);
        var comments = commentRepository.findAllByItemId(itemId);

        if (item.getOwner().getId().equals(userId)) {
            createItemDtoWithBooking(itemDtoWithBooking);
        }

        if (!comments.isEmpty()) {
            itemDtoWithBooking.setComments(comments
                    .stream().map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList()));
        }
        return itemDtoWithBooking;
    }

    @Override
    public Collection<ItemDtoWithBooking> findAll(Long userId, int from, int size) {
        checkUserAvailability(userId, userRepository);

        if (from < 0 || size <= 0) {
            throw new ValidationException("Переданы некорректные значения from and size");
        }

        var page = from / size;
        var pageable = PageRequest.of(page, size);

        var result = itemRepository.findByOwnerId(userId, pageable)
                .stream()
                .map(ItemMapper::toItemDtoWithBooking)
                .collect(Collectors.toList());

        for (ItemDtoWithBooking itemDtoWithBooking : result) {
            createItemDtoWithBooking(itemDtoWithBooking);

            var comments = commentRepository.findAllByItemId(itemDtoWithBooking.getId());

            if (!comments.isEmpty()) {
                itemDtoWithBooking.setComments(comments
                        .stream()
                        .map(CommentMapper::toCommentDto)
                        .collect(Collectors.toList()));
            }
        }
        result.sort(Comparator.comparing(ItemDtoWithBooking::getId));
        return result;
    }

    @Override
    public Collection<ItemDto> search(String text, int from, int size) {
        var page = from / size;
        var pageable = PageRequest.of(page, size);

        if (text.isEmpty()) {
            return new ArrayList<>();
        }
        return itemRepository.search(text, pageable)
                .stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto saveComment(Long userId, Long itemId, CommentDto commentDto) {
        checkUserAvailability(userId, userRepository);
        checkItemAvailability(itemId, itemRepository);

        var item = itemRepository.findById(itemId).orElseThrow();
        var user = userRepository.findById(userId).orElseThrow();

        var bookings = bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId,
                itemId,
                LocalDateTime.now(),
                Status.APPROVED);

        if (bookings.isEmpty()) {
            throw new BookingTransactionException(format("Пользователь с id: %s не брал в аренду вещь с id: %s.",
                    userId,
                    itemId));
        }

        var comment = toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(user);
        commentRepository.save(comment);
        return toCommentDto(comment);
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
