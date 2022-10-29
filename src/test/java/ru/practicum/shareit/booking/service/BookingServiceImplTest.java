package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.BookingTransactionException;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.error.exception.EnumStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.enums.Status.*;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDto;
import static ru.practicum.shareit.booking.mapper.BookingMapper.toBookingDtoSimple;

class BookingServiceImplTest {

    private BookingService bookingService;
    private BookingRepository bookingRepository;
    private ItemRepository itemRepository;
    private UserRepository userRepository;
    private Booking booking;

    @BeforeEach
    void beforeEach() {
        itemRepository = mock(ItemRepository.class);
        userRepository = mock(UserRepository.class);
        bookingRepository = mock(BookingRepository.class);
        bookingService = new BookingServiceImpl(bookingRepository,
                userRepository, itemRepository);
        booking = createBooking();
    }

    private Booking createBooking() {
        User owner = new User(1L, "user1", "user1@mail.ru");
        User booker = new User(2L, "user2", "user2@mail.ru");
        Item item = new Item(1L, "item", "description",
                true, owner, null);
        LocalDateTime start = LocalDateTime.parse("2022-09-10T10:42");
        LocalDateTime end = LocalDateTime.parse("2022-09-12T10:42");
        booking = new Booking(1L, start,
                end, item, booker, APPROVED);
        return booking;
    }

    @Test
    void saveBookingTest() {
        booking.setStatus(WAITING);
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(itemRepository.existsById(booking.getItem().getId())).thenReturn(true);

        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(itemRepository.findById(booking.getItem().getId()))
                .thenReturn(Optional.of(booking.getItem()));
        when(bookingRepository.save(any(Booking.class)))
                .thenReturn(booking);
        var bookingDto = bookingService.save(toBookingDtoSimple(booking),
                booking.getBooker().getId());
        assertNotNull(bookingDto);
        assertEquals("item", bookingDto.getItem().getName());
        assertEquals("user2", bookingDto.getBooker().getName());
        assertEquals(booking.getId(), bookingDto.getId());
        // verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void updateBookingTest() {
        Booking booking2 = createBooking();
        long bookingId = booking.getId();
        booking2.setStatus(REJECTED);

        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);

        when(bookingRepository.save(any(Booking.class))).thenReturn(booking2);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));
        BookingDto bookingDto = bookingService.update(bookingId,
                toBookingDto(booking2));
        assertNotNull(bookingDto);
        assertEquals(REJECTED, bookingDto.getStatus());
        assertEquals(booking.getId(), bookingDto.getId());
        // verify(bookingRepository, times(1)).save(booking2);
    }

    @Test
    void deleteBookingByIdTest() {
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);

        bookingService.deleteById(booking.getId());

        verify(bookingRepository, times(1)).deleteById(booking.getId());
    }

    @Test
    void findBookingByIdTest() {
        var bookingId = booking.getId();
        long incorrectId = (long) (Math.random() * 100) + bookingId + 3;

        when(bookingRepository.existsById(booking.getId())).thenReturn(true);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.findById(incorrectId))
                .thenThrow(new EntityNotFoundException("Бронирования с Id = " + incorrectId + " нет в БД"));

        var bookingDto = bookingService.findById(bookingId, booking.getBooker().getId());

        assertNotNull(bookingDto);
        assertEquals("item", bookingDto.getItem().getName());

        var exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.findById(incorrectId, booking.getBooker().getId()));
        assertNotNull(exception.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);

        long incorrectUserId = 10L;
        var exception1 = assertThrows(EntityNotFoundException.class,
                () -> bookingService.findById(bookingId,
                        incorrectUserId));
        assertNotNull(exception1.getMessage());
    }

    @Test
    void findAllBookingsTest() {
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findByBookerId(booking.getBooker().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookingDtoList = new ArrayList<>(bookingService
                .findAll(booking.getBooker().getId(), "ALL", 0, 20));

        assertNotNull(bookingDtoList);
        assertEquals(1, bookingDtoList.size());
        assertEquals(booking.getItem().getName(), bookingDtoList.get(0).getItem().getName());
        verify(bookingRepository, times(1))
                .findByBookerId(booking.getBooker().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));
    }

    @Test
    void findAllByStatusWaitingTest() {
        booking.setStatus(WAITING);
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findBookingsByBookerIdAndStatus(booking.getBooker().getId(),
                WAITING,
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAll(booking.getBooker().getId(),
                        "WAITING", 0, 20));

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking.getStatus(), bookings.get(0).getStatus());
        verify(bookingRepository, times(1))
                .findBookingsByBookerIdAndStatus(booking.getBooker().getId(),
                        WAITING,
                        PageRequest.of(0, 20, Sort.by("start").descending()));
    }

    @Test
    void findAllByStatusRejectTest() {
        booking.setStatus(REJECTED);
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(booking.getBooker().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findBookingsByBookerIdAndStatus(booking.getBooker().getId(),
                REJECTED,
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAll(booking.getBooker().getId(),
                        "REJECTED", 0, 20));

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(booking.getStatus(), bookings.get(0).getStatus());
        verify(bookingRepository, times(1))
                .findBookingsByBookerIdAndStatus(booking.getBooker().getId(),
                        REJECTED,
                        PageRequest.of(0, 20, Sort.by("start").descending()));

        var incorrectState = "error";
        var exception = assertThrows(EnumStateException.class,
                () -> bookingService.findAll(booking.getBooker().getId(),
                        incorrectState, 0, 20));

        assertNotNull(exception.getMessage());
    }

    @Test
    void getAllByBookerWithStatePast() {
        final LocalDateTime date = LocalDateTime.now();
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findBookingsByBookerIdAndEndIsBefore(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService.findAll(2L, "PAST", 0, 2));

        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
        assertTrue(bookings.get(0).getEnd().isBefore(date));
        verify(bookingRepository, times(1))
                .findBookingsByBookerIdAndEndIsBefore(anyLong(), any(), any());
    }

    @Test
    void getAllByBookerWithStateFuture() {
        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.findByBookerIdAndStartAfter(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService.findAll(2L, "FUTURE", 0, 2));

        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
    }

    @Test
    void getAllByBookerWithStateCurrent() {
        final var booker = new User(2L, "BookerName", "booker@mail.ru");

        when(userRepository.existsById(booking.getBooker().getId())).thenReturn(true);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookingsByBookerId(anyLong(), any(), any()))
                .thenReturn(Collections.singletonList(booking));

        List<BookingDto> bookings = new ArrayList<>(bookingService.findAll(2L, "CURRENT", 0, 2));
        assertNotEquals(bookings, null);
        assertEquals(bookings.size(), 1);
    }

    @Test
    void localDateTimeTest() {
        var errorEnd = booking.getEnd().minusDays(30);
        booking.setEnd(errorEnd);
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);

        Throwable thrown = assertThrows(BookingTransactionException.class,
                () -> bookingService.save(toBookingDtoSimple(booking),
                        booking.getBooker().getId()));
        assertNotNull(thrown.getMessage());
    }

    @Test
    void approveBookingTest() {
        var bookingId = booking.getId();

        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        var exception = assertThrows(BookingTransactionException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(),
                        bookingId, true));
        assertNotNull(exception.getMessage());
        verify(bookingRepository, times(1)).findById(bookingId);

        var exception1 = assertThrows(BookingTransactionException.class,
                () -> bookingService.approve(booking.getItem().getOwner().getId(),
                        bookingId, null));
        assertNotNull(exception1.getMessage());
    }

    @Test
    void approveBookingWithApprovedStateShouldThrowExceptionTest() {
        final LocalDateTime date = LocalDateTime.now();
        final User owner = new User(1L, "UserName", "user@mail.ru");
        final Item item = new Item(1L, "ItemName", "ItemDesc", true, owner, null);
        var bookingNew = new Booking(1L, date.minusDays(1), date.minusHours(1), item, owner, APPROVED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(bookingNew));

        var exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.approve(2L, 1L, true));

        assertNotNull(exception.getMessage());
    }

    @Test
    void approveBookingWhenApproveIsNullShouldThrowExceptionTest() {
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        var exception = assertThrows(BookingTransactionException.class,
                () -> bookingService.approve(1L, 1L, null));

        assertNotNull(exception.getMessage());
    }

    @Test
    void approveBookingBranchesTest() {
        booking.setStatus(WAITING);

        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        var expected = bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(),
                true);

        assertEquals(booking.getId(), expected.getId());
        assertEquals(booking.getItem().getId(), expected.getItem().getId());
        assertEquals(booking.getStart(), expected.getStart());
        assertEquals(booking.getEnd(), expected.getEnd());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void rejectBookingTest() {
        booking.setStatus(WAITING);

        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(bookingRepository.existsById(booking.getId())).thenReturn(true);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        var expected = bookingService.approve(booking.getItem().getOwner().getId(), booking.getId(),
                false);

        assertEquals(booking.getId(), expected.getId());
        assertEquals(booking.getItem().getId(), expected.getItem().getId());
        assertEquals(booking.getStart(), expected.getStart());
        assertEquals(booking.getEnd(), expected.getEnd());

        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void findAllByItemOwnerIdTest() {
        var bookingDto = toBookingDto(booking);

        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var expected = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "ALL", 0, 20));

        assertNotNull(expected);
        assertEquals(1, expected.size());
        assertEquals(bookingDto, expected.get(0));
        verify(bookingRepository, times(1))
                .searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                        PageRequest.of(0, 20, Sort.by("start").descending()));
    }

    @Test
    void getAllByOwnerIdWithStateFutureTest() {
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "FUTURE", 0, 20));

        assertNotEquals(bookings, null);
    }

    @Test
    void getAllByOwnerIdWithStatePastTest() {
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "PAST", 0, 20));

        assertNotEquals(bookings, null);
    }

    @Test
    void getAllByOwnerIdWithStateWaitingTest() {
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "WAITING", 0, 20));

        assertNotEquals(bookings, null);
    }

    @Test
    void getAllByOwnerIdWithStateRejectedTest() {
        var incorrectState = "error";
        var exception = assertThrows(EntityNotFoundException.class,
                () -> bookingService.findAllByItemOwnerId(booking.getBooker().getId(),
                        incorrectState, 0, 20));

        assertNotNull(exception.getMessage());

        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "REJECTED", 0, 20));

        assertNotEquals(bookings, null);
    }

    @Test
    void getAllByOwnerIdWithStateCurrentTest() {
        when(userRepository.existsById(booking.getItem().getOwner().getId())).thenReturn(true);
        when(userRepository.findById(booking.getItem().getOwner().getId()))
                .thenReturn(Optional.of(booking.getBooker()));
        when(bookingRepository.searchBookingByItemOwnerId(booking.getItem().getOwner().getId(),
                PageRequest.of(0, 20, Sort.by("start").descending())))
                .thenReturn(Collections.singletonList(booking));

        var bookings = new ArrayList<>(bookingService
                .findAllByItemOwnerId(booking.getItem().getOwner().getId(),
                        "CURRENT", 0, 20));

        assertNotEquals(bookings, null);
    }
}