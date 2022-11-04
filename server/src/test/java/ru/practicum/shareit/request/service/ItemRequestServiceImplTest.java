package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequestDto;

class ItemRequestServiceImplTest {

    private ItemRequestRepository itemRequestRepository;
    private UserRepository userRepository;
    private ItemRequestService itemRequestService;
    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        itemRequestRepository = mock(ItemRequestRepository.class);
        var itemRepository = mock(ItemRepository.class);
        var itemRequestMapper = new ItemRequestMapper(itemRepository);
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, itemRequestMapper, userRepository);
        var user = new User(1L, "user", "user@mail.ru");
        itemRequest = new ItemRequest(1L, "itemRequest", LocalDateTime.now(), user);
    }

    @Test
    void saveItemRequestTest() {
        when(userRepository.existsById(itemRequest.getUser().getId())).thenReturn(true);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);
        when(userRepository.findById(itemRequest.getUser().getId())).thenReturn(Optional.of(itemRequest.getUser()));

        var expected = itemRequestService.save(toItemRequestDto(itemRequest), itemRequest.getUser().getId());

        assertNotNull(expected);
        assertEquals("itemRequest", expected.getDescription());
        assertEquals("user", itemRequest.getUser().getName());
        assertEquals(itemRequest.getId(), expected.getId());
        verify(itemRequestRepository, times(1)).save(any(ItemRequest.class));
    }

    @Test
    void findAllRequestsTest() {
        when(userRepository.existsById(itemRequest.getUser().getId())).thenReturn(true);
        when(userRepository.findById(itemRequest.getUser().getId())).thenReturn(Optional.of(itemRequest.getUser()));
        when(itemRequestRepository
                .findAllByUserIdOrderByCreatedDesc(itemRequest.getUser().getId()))
                .thenReturn(Collections.singletonList(itemRequest));

        final var itemRequestDtoWithItems = new ArrayList<>(itemRequestService
                .findAll(itemRequest.getUser().getId()));

        assertNotNull(itemRequestDtoWithItems);
        assertEquals(1, itemRequestDtoWithItems.size());
        assertEquals(itemRequest.getDescription(), itemRequestDtoWithItems.get(0).getDescription());
        verify(itemRequestRepository, times(1))
                .findAllByUserIdOrderByCreatedDesc(itemRequest.getUser().getId());
    }

    @Test
    void findRequestByIdTest() {
        var itemRequestId = itemRequest.getId();
        when(userRepository.existsById(itemRequest.getUser().getId())).thenReturn(true);
        when(itemRequestRepository.existsById(itemRequestId)).thenReturn(true);
        when(userRepository.findById(itemRequest.getUser().getId()))
                .thenReturn(Optional.of(itemRequest.getUser()));

        var incorrectId = (long) (Math.random() * 100) + itemRequestId + 3;

        when(itemRequestRepository.findById(itemRequestId)).thenReturn(Optional.of(itemRequest));
        when(itemRequestRepository.findById(incorrectId))
                .thenThrow(new EntityNotFoundException("Запроса с Id = " + incorrectId + " нет в БД"));

        var itemRequestDtoWithItems = itemRequestService
                .findByRequestId(itemRequest.getUser().getId(), itemRequestId);

        assertNotNull(itemRequestDtoWithItems);
        assertEquals("itemRequest", itemRequestDtoWithItems.getDescription());

        var exception = assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.findByRequestId(itemRequest.getUser().getId(), incorrectId));
        assertNotNull(exception.getMessage());
        verify(itemRequestRepository, times(1)).findById(itemRequestId);
    }

    @Test
    void findAllWithPageableRequestsTest() {
        when(userRepository.existsById(itemRequest.getUser().getId())).thenReturn(true);
        when(userRepository.findById(itemRequest.getUser().getId())).thenReturn(Optional.of(itemRequest.getUser()));
        when(itemRequestRepository
                .findAll(PageRequest.of(0, 20, Sort.by("created"))))
                .thenReturn(Page.empty());

        final var itemRequestDtoWithItems = new ArrayList<>(itemRequestService
                .findAllRequests(itemRequest.getUser().getId(), 0, 20));

        assertNotNull(itemRequestDtoWithItems);
        assertTrue(itemRequestDtoWithItems.isEmpty());
        verify(itemRequestRepository, times(1))
                .findAll(PageRequest.of(0, 20, Sort.by("created")));
    }

    @Test
    void getAllItemRequestsTest() {
        when(userRepository.existsById(itemRequest.getUser().getId())).thenReturn(true);
        when(userRepository.findById(itemRequest.getUser().getId())).thenReturn(Optional.of(itemRequest.getUser()));
        when(itemRequestRepository
                .findAllByUserIdOrderByCreatedDesc(itemRequest.getUser().getId()))
                .thenReturn(Collections.singletonList(itemRequest));

        final var itemRequestDtoWithItems = new ArrayList<>(itemRequestService
                .findAll(itemRequest.getUser().getId()));

        assertNotNull(itemRequestDtoWithItems);
        assertEquals(1, itemRequestDtoWithItems.size());
    }
}
