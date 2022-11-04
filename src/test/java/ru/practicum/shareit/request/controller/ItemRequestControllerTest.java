package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequestDto;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;
    @InjectMocks
    private ItemRequestController itemRequestController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private ItemRequestMapper itemRequestMapper;

    private ItemRequest itemRequest;

    @BeforeEach
    void setUp() {
        var itemRepository = mock(ItemRepository.class);
        itemRequestMapper = new ItemRequestMapper(itemRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(itemRequestController).build();
        mapper.registerModule(new JavaTimeModule());
        var user = new User(2L, "user", "user@mail.ru");
        itemRequest = new ItemRequest(1L, "itemRequest1", LocalDateTime.now(), user);
    }

    @Test
    void saveItemRequestTest() throws Exception {
        var itemRequestDto = toItemRequestDto(itemRequest);
        when(itemRequestService.save(any(), any())).thenReturn(itemRequestDto);

        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequestDto.getId()))
                .andExpect(jsonPath("$.description").value(itemRequestDto.getDescription()));
    }

    @Test
    void findAllItemRequestsTest() throws Exception {
        var itemRequestDtoWithItems = itemRequestMapper.toItemRequestDtoWithItems(itemRequest);

        when(itemRequestService.findAll(anyLong())).thenReturn(Collections.singletonList(itemRequestDtoWithItems));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", itemRequest.getUser().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDtoWithItems.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDtoWithItems.getDescription()));

        verify(itemRequestService, times(1)).findAll(itemRequest.getUser().getId());
    }

    @Test
    void findItemRequestsByIdTest() throws Exception {
        var itemRequestDtoWithItems = itemRequestMapper.toItemRequestDtoWithItems(itemRequest);
        when(itemRequestService.findAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(itemRequestDtoWithItems));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", itemRequest.getUser().getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(itemRequestDtoWithItems.getId()))
                .andExpect(jsonPath("$[0].description").value(itemRequestDtoWithItems.getDescription()))
                .andExpect(content().json("[{\"id\": 1, \"description\": \"itemRequest1\"}]"));

        verify(itemRequestService, times(1))
                .findAllRequests(itemRequest.getUser().getId(), 0, 20);
    }

    @Test
    void findItemRequestByRequestIdTest() throws Exception {
        var itemRequestDtoWithItems = itemRequestMapper.toItemRequestDtoWithItems(itemRequest);
        when(itemRequestService.findByRequestId(any(), any())).thenReturn(itemRequestDtoWithItems);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", itemRequest.getUser().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemRequest.getId()))
                .andExpect(jsonPath("$.description").value(itemRequest.getDescription()));

        verify(itemRequestService, times(1))
                .findByRequestId(itemRequest.getUser().getId(), itemRequest.getId());
    }
}
