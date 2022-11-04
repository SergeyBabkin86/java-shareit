package ru.practicum.shareit.booking.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;
import ru.practicum.shareit.booking.dto.BookingDtoSimple;
import ru.practicum.shareit.booking.model.Booking;

@Component
public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        return new BookingDto(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem(),
                booking.getBooker(),
                booking.getStatus());
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return new Booking(bookingDto.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                bookingDto.getItem(),
                bookingDto.getBooker(),
                bookingDto.getStatus());
    }

    public static Booking fromSimpleDtoToBooking(BookingDtoSimple bookingDtoSimple) {
        return new Booking(bookingDtoSimple.getId(),
                bookingDtoSimple.getStart(),
                bookingDtoSimple.getEnd(),
                null,
                null,
                null);
    }

    public static BookingDtoSimple toBookingDtoSimple(Booking booking) {
        return new BookingDtoSimple(booking.getId(),
                booking.getStart(),
                booking.getEnd(),
                booking.getItem().getId());
    }

    public static BookingDtoForItem toBookingDtoForItem(Booking booking) {
        return new BookingDtoForItem(booking.getId(),
                booking.getBooker().getId());
    }
}