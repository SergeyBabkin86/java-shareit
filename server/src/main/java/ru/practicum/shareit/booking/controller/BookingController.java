package ru.practicum.shareit.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.BookingDto;
import ru.practicum.shareit.booking.model.BookingDtoSimple;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto create(@RequestBody BookingDtoSimple bookingDtoSimple,
                             @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.save(bookingDtoSimple, userId);
    }

    @GetMapping
    public Collection<BookingDto> findAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                          @RequestParam(defaultValue = "ALL") String state,
                                          @RequestParam(defaultValue = "0") int from,
                                          @RequestParam(defaultValue = "20") int size) {
        return bookingService.findAll(userId, state, from, size);
    }

    @GetMapping("/owner")
    public Collection<BookingDto> findAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @RequestParam(defaultValue = "ALL") String state,
                                                 @RequestParam(defaultValue = "0") int from,
                                                 @RequestParam(defaultValue = "20") int size) {
        return bookingService.findAllByItemOwnerId(userId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader("X-Sharer-User-Id") long userId,
                              @PathVariable long bookingId,
                              @RequestParam Boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{id}")
    public BookingDto findById(@PathVariable long id,
                               @RequestHeader("X-Sharer-User-Id") long userId) {
        return bookingService.findById(id, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable long id) {
        bookingService.deleteById(id);
    }
}
