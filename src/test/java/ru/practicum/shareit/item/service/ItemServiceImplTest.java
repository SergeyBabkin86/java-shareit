package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.item.mapper.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;

@Transactional
@SpringBootTest
public class ItemServiceImplTest {
    private UserService userService;
    private ItemRequestRepository itemRequestRepository;
    private ItemRepository itemRepository;
    private BookingRepository bookingRepository;
    private CommentRepository commentRepository;
    private UserRepository userRepository;

    private ItemService itemService;

    private Item item;
    private User booker;
    private User owner;
    private ItemRequest itemRequest;
    private Comment comment;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        itemRepository = mock(ItemRepository.class);
        userService = mock(UserService.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        bookingRepository = mock(BookingRepository.class);
        commentRepository = mock(CommentRepository.class);
        userRepository = mock(UserRepository.class);

        itemService = new ItemServiceImpl(
                itemRepository, userRepository, bookingRepository, commentRepository, itemRequestRepository);

        booker = new User(2L, "user2", "user2@mail.ru");
        owner = new User(1L, "user1", "user1@mail.ru");

        itemRequest = new ItemRequest(1L, "description1", LocalDateTime.now(), booker);
        item = new Item(1L, "ВещьТест", "Описание вещи", true, owner, itemRequest);
        lastBooking = new Booking(1L, LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5),
                item, booker, Status.APPROVED);
        nextBooking = new Booking(2L, LocalDateTime.now().plusDays(5), LocalDateTime.now().plusDays(10),
                item, booker, Status.APPROVED);
        comment = new Comment(1L, "Комментарий", item, booker, LocalDateTime.now());

    }

    @Test
    void saveItemTest() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(owner));
        when(itemRequestRepository.existsById(itemRequest.getId())).thenReturn(true);
        when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequest));
        when(itemRepository.save(any())).thenReturn(item);

        var expected = itemService.save(owner.getId(), toItemDto(item));

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(item.getId()));
        assertThat(expected.getName(), equalTo(item.getName()));
        assertThat(expected.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void updateItemTest() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(itemRepository.existsById(item.getId())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.save(any())).thenReturn(item);

        var actual = new ItemDto(1L, "Обновленное имя", "Обновленное описание",
                false, null);

        var expected = itemService.update(owner.getId(), item.getId(), actual);

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(actual.getId()));
        assertThat(expected.getName(), equalTo(actual.getName()));
        assertThat(expected.getDescription(), equalTo(actual.getDescription()));
    }

    @Test
    void updateItemWithNullFieldsTest() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(itemRepository.existsById(item.getId())).thenReturn(true);
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(owner));
        when(itemRepository.save(any())).thenReturn(item);

        var actual = new ItemDto(1L, null, null, null, null);

        var expected = itemService.update(owner.getId(), item.getId(), actual);

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(item.getId()));
        assertThat(expected.getName(), equalTo(item.getName()));
        assertThat(expected.getDescription(), equalTo(item.getDescription()));
    }

    @Test
    void deleteItemTest() {
        when(itemRepository.existsById(item.getId())).thenReturn(true);

        itemService.deleteById(item.getId());

        verify(itemRepository, times(1)).deleteById(item.getId());
    }

    @Test
    void findItemByIdTest() {
        long incorrectId = (long) (Math.random() * 100) + item.getId() + 3;

        when(itemRepository.existsById(item.getId())).thenReturn(true);
        when(userRepository.existsById(owner.getId())).thenReturn(true);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.findById(incorrectId))
                .thenThrow(new EntityNotFoundException(format("Вещь с id: %s не найдена.", item.getId())));

        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(owner));

        var expected = itemService.findById(item.getId(), owner.getId());

        assertThat(expected, notNullValue());
        assertThat(expected.getId(), equalTo(item.getId()));
        assertThat(expected.getName(), equalTo(item.getName()));
        assertThat(expected.getDescription(), equalTo(item.getDescription()));

        Throwable thrown = assertThrows(EntityNotFoundException.class,
                () -> itemService.findById(incorrectId, item.getOwner().getId()));
        assertNotNull(thrown.getMessage());
        verify(itemRepository, times(1)).findById(item.getId());
    }

    @Test
    void findAllItemsTest() {
        when(userRepository.existsById(owner.getId())).thenReturn(true);
        when(commentRepository.findAllByItemId(item.getId()))
                .thenReturn(Collections.singletonList(comment));
        when(itemRepository.findByOwnerId(item.getOwner().getId(), PageRequest.of(0, 20)))
                .thenReturn(Collections.singletonList(item));

        var expected = new ArrayList<>(itemService.findAll(item.getOwner().getId(), 0, 20));

        assertThat(expected, hasItems());
        assertEquals(1, expected.size());
        assertEquals(item.getName(), expected.get(0).getName());
        verify(itemRepository, times(1))
                .findByOwnerId(item.getOwner().getId(), PageRequest.of(0, 20));
    }

    @Test
    void searchItemsTest() {
        var pageable = PageRequest.of(0, 10);
        when(itemRepository.search("ние вещ", pageable)).thenReturn(Collections.singletonList(item));

        var actual = toItemDto(item);

        var expected = new ArrayList<>(itemService.search("ние вещ", 0, 10));

        assertThat(expected, hasItems());
        assertThat(expected.get(0).getId(), equalTo(actual.getId()));
        assertThat(expected.get(0).getDescription(), equalTo(actual.getDescription()));
    }

    @Test
    void searchItemsWithEmptyTextTest() {
        List<ItemDto> expected = new ArrayList<>(itemService.search("", 0, 10));

        assertThat(expected, emptyCollectionOf(ItemDto.class));
    }

    @Test
    void saveCommentForItemTest() {
        when(userRepository.existsById(booker.getId())).thenReturn(true);
        when(itemRepository.existsById(item.getId())).thenReturn(true);

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        List<Booking> bookingsList = new ArrayList<>();
        bookingsList.add(lastBooking);
        when(bookingRepository
                .searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(anyLong(), anyLong(), any(), any()))
                .thenReturn(bookingsList);
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentDto commentDto1 = toCommentDto(comment);
        CommentDto expected = itemService.saveComment(booker.getId(), item.getId(),
                commentDto1);

        assertThat(expected, notNullValue());
        assertThat(expected.getText(), equalTo(comment.getText()));
        assertThat(expected.getAuthorName(), equalTo(comment.getAuthor().getName()));
        assertThat(expected.getId(), equalTo(comment.getId()));

        verify(commentRepository, times(1)).save(any());
    }

    @Test
    void createCommentWithEmptyTextTest() {
        when(userService.getById(anyLong())).thenReturn(UserMapper.toUserDto(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        var actual = CommentMapper.toCommentDto(comment);
        actual.setText("");

        assertThrows(EntityNotFoundException.class,
                () -> itemService.saveComment(booker.getId(), item.getId(), actual));
    }

    @Test
    void createCommentWithNoBookingTest() {
        when(userService.getById(anyLong())).thenReturn(UserMapper.toUserDto(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(bookingRepository.searchBookingByBookerIdAndItemIdAndEndIsBeforeAndStatus(anyLong(), anyLong(), any(), any()))
                .thenReturn(Collections.emptyList());

        assertThrows(EntityNotFoundException.class,
                () -> itemService.saveComment(booker.getId(), item.getId(), CommentMapper.toCommentDto(comment)));
    }

    @Test
    void exceptionIncorrectUserIdTest() {
        var commentDto = toCommentDto(comment);
        long incorrectUserId = 10L;
        Throwable thrown = assertThrows(EntityNotFoundException.class,
                () -> itemService.saveComment(incorrectUserId,
                        item.getOwner().getId(), commentDto));

        assertNotNull(thrown.getMessage());
    }

    @Test
    void exceptionIncorrectItemIdTest() {
        var commentDto = toCommentDto(comment);
        long incorrectItemId = 10L;
        Throwable thrown2 = assertThrows(EntityNotFoundException.class,
                () -> itemService.saveComment(booker.getId(),
                        incorrectItemId, commentDto));
        assertNotNull(thrown2.getMessage());
    }

    @Test
    void updateItemExceptionUserTest() {
        var item2 = this.item;
        var incorrectUserId = 10L;
        item2.setName("item2");

        when(itemRepository.save(any(Item.class))).thenReturn(item2);
        Throwable thrown = assertThrows(EntityNotFoundException.class,
                () -> itemService.update(
                        incorrectUserId,
                        incorrectUserId, toItemDto(item2)));

        assertNotNull(thrown.getMessage());
    }

    @Test
    void updateItemExceptionItemTest() {
        var item2 = this.item;
        var incorrectUserId = 10L;
        item2.setName("item2");

        when(itemRepository.save(any(Item.class))).thenReturn(item2);
        Throwable thrown = assertThrows(EntityNotFoundException.class,
                () -> itemService.update(
                        item.getId(),
                        incorrectUserId, toItemDto(item2)));

        assertNotNull(thrown.getMessage());
    }
}
