package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.BookingTransactionException;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.error.exception.EnumStateException;
import ru.practicum.shareit.error.exception.ItemTransactionException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static ru.practicum.shareit.booking.mapper.BookingMapper.*;
import static ru.practicum.shareit.utilities.Checker.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDto save(BookingDtoSimple bookingDtoSimple, Long userId) {
        checkUserAvailability(userId, userRepository);
        checkBookingEndTime(bookingDtoSimple.getStart(), bookingDtoSimple.getEnd());
        checkItemAvailability(bookingDtoSimple.getItemId(), itemRepository);

        var item = itemRepository.findById(bookingDtoSimple.getItemId()).get();
        if (!item.getAvailable()) {
            throw new ItemTransactionException(format("Вещь с id: %s не доступна.", item.getId()));
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Владелец вещи не может забронировать свою вещь.");
        }

        var booking = fromSimpleDtoToBooking(bookingDtoSimple);

        booking.setBooker(userRepository.findById(userId).get());
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        return mapToBookingDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto update(Long bookingId, BookingDto bookingDto) {
        checkBookingAvailability(bookingId, bookingRepository);

        var oldBookingDto = mapToBookingDto(bookingRepository.findById(bookingId).get());

        if (bookingDto.getStart() != null) {
            oldBookingDto.setStart(bookingDto.getStart());
        }
        if (bookingDto.getEnd() != null) {
            oldBookingDto.setEnd(bookingDto.getEnd());
        }
        if (bookingDto.getItem() != null) {
            oldBookingDto.setItem(bookingDto.getItem());
        }
        if (bookingDto.getBooker() != null) {
            oldBookingDto.setBooker(bookingDto.getBooker());
        }
        if (bookingDto.getStatus() != null) {
            oldBookingDto.setStatus(bookingDto.getStatus());
        }
        return mapToBookingDto(bookingRepository.save(mapToBooking(oldBookingDto)));
    }

    @Override
    public void deleteById(Long bookingId) {
        checkBookingAvailability(bookingId, bookingRepository);
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingDto findById(Long bookingId, Long userId) {
        checkBookingAvailability(bookingId, bookingRepository);
        var booking = bookingRepository.findById(bookingId).get();

        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Запрос бронирования может быть выполнен только автором бронирования " +
                    "или владельцем вещи, к которой относится бронирование.");
        }
        return mapToBookingDto(booking);
    }

    @Override
    public Collection<BookingDto> findAll(Long userId, String state) {
        checkUserAvailability(userId, userRepository);
        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    return bookingRepository.findByBookerIdOrderByStartDesc(userId)
                            .stream().map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByBookerIdOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(BookingMapper::mapToBookingDto)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByBookerIdAndEndIsBeforeOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(BookingMapper::mapToBookingDto)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId,
                                    LocalDateTime.now())
                            .stream()
                            .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByBookerIdAndStatusOrderByStartDesc(userId,
                                    Status.WAITING)
                            .stream()
                            .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByBookerIdAndStatusOrderByStartDesc(userId,
                                    Status.REJECTED)
                            .stream()
                            .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                default:
                    throw new EnumStateException(format("Unknown state: %S", state));
            }

        } catch (IllegalArgumentException e) {
            throw new EnumStateException(format("Unknown state: %S", state));
        }
    }

    @Override
    public Collection<BookingDto> findAllByItemOwnerId(Long userId, String state) {
        checkUserAvailability(userId, userRepository);

        List<BookingDto> result = bookingRepository.searchBookingByItemOwnerId(userId)
                .stream()
                .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());

        if (result.isEmpty()) {
            throw new EntityNotFoundException(format("У пользователя c id: %s нет вещей.", userId));
        }
        try {
            switch (Status.valueOf(state)) {
                case ALL:
                    result.sort(Comparator.comparing(BookingDto::getStart).reversed());
                    return result;
                case CURRENT:
                    return bookingRepository.findCurrentBookingsByItemOwnerIdOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(BookingMapper::mapToBookingDto)
                            .collect(Collectors.toList());
                case PAST:
                    return bookingRepository.findBookingsByItemOwnerIdAndEndIsBeforeOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(BookingMapper::mapToBookingDto)
                            .collect(Collectors.toList());
                case FUTURE:
                    return bookingRepository.searchBookingByItemOwnerIdAndStartIsAfterOrderByStartDesc(userId,
                                    LocalDateTime.now()).stream().map(BookingMapper::mapToBookingDto)
                            .collect(Collectors.toList());
                case WAITING:
                    return bookingRepository.findBookingsByItemOwnerIdOrderByStartDesc(userId).stream()
                            .filter(booking -> booking.getStatus().equals(Status.WAITING))
                            .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                case REJECTED:
                    return bookingRepository.findBookingsByItemOwnerIdOrderByStartDesc(userId).stream()
                            .filter(booking -> booking.getStatus().equals(Status.REJECTED))
                            .map(BookingMapper::mapToBookingDto).collect(Collectors.toList());
                default:
                    throw new EnumStateException(format("Unknown state: %S", state));
            }
        } catch (IllegalArgumentException e) {
            throw new EnumStateException(format("Unknown state: %S", state));
        }
    }

    @Override
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        checkUserAvailability(userId, userRepository);
        checkBookingAvailability(bookingId, bookingRepository);

        var bookingDto = mapToBookingDto(bookingRepository.findById(bookingId).get());

        if (!bookingDto.getItem().getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Подтвердить бронирование может только владелец вещи.");
        }
        if (bookingDto.getStatus().equals(Status.APPROVED)) {
            throw new BookingTransactionException("Бронирование уже подтверждено.");
        }
        if (approved == null) {
            throw new BookingTransactionException("Не указан статус бронирования.");
        } else if (approved) {
            bookingDto.setStatus(Status.APPROVED);
            return mapToBookingDto(bookingRepository.save(mapToBooking(bookingDto)));
        } else {
            bookingDto.setStatus(Status.REJECTED);
            return mapToBookingDto(bookingRepository.save(mapToBooking(bookingDto)));
        }
    }

    private void checkBookingEndTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new BookingTransactionException("Дата окончания бронирования не может быть ранее даты начала");
        }
    }
}
