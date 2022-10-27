package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;

import java.util.Collection;

public interface BookingService {
    BookingDto save(BookingDtoSimple bookingDtoSimple, Long userId);

    BookingDto update(Long bookingId, BookingDto bookingDto);

    void deleteById(Long bookingId);

    BookingDto findById(Long bookingId, Long userId);

    Collection<BookingDto> findAll(Long userId, String state);

    Collection<BookingDto> findAllByItemOwnerId(Long userId, String state);

    BookingDto approve(Long userId, Long bookingId, Boolean approved);
}
