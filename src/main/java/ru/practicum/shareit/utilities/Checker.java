package ru.practicum.shareit.utilities;

import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import static java.lang.String.format;

public class Checker {

    public static void checkUserAvailability(Long userId, UserRepository userRepository) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(format("Пользователь с id: %s не найден.", userId));
        }
    }

    public static void checkItemAvailability(Long itemId, ItemRepository itemRepository) {
        if (!itemRepository.existsById(itemId)) {
            throw new EntityNotFoundException(format("Вещь с id: %s не найдена.", itemId));
        }
    }

    public static void checkBookingAvailability(Long bookingId, BookingRepository bookingRepository) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new EntityNotFoundException(format("Бронирование с id: %s не найдена.", bookingId));
        }
    }
}
