package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.item.mapper.CommentMapper.toCommentDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDto;
import static ru.practicum.shareit.item.mapper.ItemMapper.toItemDtoWithBooking;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    @Mock
    private ItemService itemService;
    @InjectMocks
    private ItemController itemController;
    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();
    private Item item;
    private Comment comment;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .build();
        mapper.registerModule(new JavaTimeModule());

        User owner = new User(1L, "user1", "user1@mail.ru");
        User booker = new User(2L, "user2", "user2@mail.ru");
        ItemRequest itemRequest = new ItemRequest(1L, "itemRequest1",
                LocalDateTime.now(), booker);
        item = new Item(1L, "item1", "description1", true, owner, itemRequest);
        comment = new Comment(1L, "Комментарий", item, booker, LocalDateTime.now());
    }

    @Test
    void saveItemTest() throws Exception {
        var itemDto = toItemDto(item);
        when(itemService.save(item.getOwner().getId(), itemDto)).thenReturn(itemDto);

        mockMvc.perform(post("/items").content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemDto.getRequestId()));

        verify(itemService, times(1)).save(item.getOwner().getId(), itemDto);
    }

    @Test
    void updateItemTest() throws Exception {
        var itemDto = toItemDto(item);
        var item2 = this.item;
        var itemDto2 = toItemDto(item2);
        itemDto2.setName("item2");

        itemService.save(item.getOwner().getId(), itemDto);
        when(itemService.update(item.getOwner().getId(), itemDto.getId(), itemDto2)).thenReturn(itemDto2);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .content(mapper.writeValueAsString(itemDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDto.getId()))
                .andExpect(jsonPath("$.name").value(itemDto2.getName()))
                .andExpect(jsonPath("$.description").value(itemDto.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDto.getAvailable()))
                .andExpect(jsonPath("$.requestId").value(itemDto.getRequestId()));

        verify(itemService, times(1))
                .update(item.getOwner().getId(), itemDto.getId(), itemDto2);
    }

    @Test
    void deleteItemTest() throws Exception {
        mockMvc.perform(delete("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void findItemByIdTest() throws Exception {
        var itemDtoWithBooking = toItemDtoWithBooking(item);
        when(itemService.findById(1L, item.getOwner().getId())).thenReturn(itemDtoWithBooking);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", item.getOwner().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDtoWithBooking.getId()))
                .andExpect(jsonPath("$.name").value(itemDtoWithBooking.getName()))
                .andExpect(jsonPath("$.description").value(itemDtoWithBooking.getDescription()))
                .andExpect(jsonPath("$.available").value(itemDtoWithBooking.getAvailable()))
                .andExpect(jsonPath("$.lastBooking").value(itemDtoWithBooking.getLastBooking()))
                .andExpect(jsonPath("$.nextBooking").value(itemDtoWithBooking.getNextBooking()))
                .andExpect(jsonPath("$.comments").value(itemDtoWithBooking.getComments()));

        verify(itemService, times(1)).findById(1L, 1L);
    }

    @Test
    void findAllItemsTest() throws Exception {
        List<ItemDtoWithBooking> items = new ArrayList<>();
        var itemDtoWithBooking = toItemDtoWithBooking(item);
        items.add(itemDtoWithBooking);
        when(itemService.findAll(item.getOwner().getId(), 0, 20)).thenReturn(items);

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", item.getOwner().getId())
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"id\": 1,\"name\": \"item1\"," +
                        " \"description\": \"description1\", \"available\": true," +
                        " \"lastBooking\": null, \"nextBooking\": null, \"comments\": []}]"));

        verify(itemService, times(1))
                .findAll(item.getOwner().getId(), 0, 20);
    }

    @Test
    void searchItemTest() throws Exception {
        List<ItemDto> items = new ArrayList<>();
        var itemDto = toItemDto(item);
        items.add(itemDto);
        var text = item.getDescription().substring(0, 3);
        when(itemService.search(text, 0, 20)).thenReturn(items);

        mockMvc.perform(get("/items/search")
                        .param("text", text)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(itemDto.getName()))
                .andExpect(jsonPath("$[0].description").value(itemDto.getDescription()));

        verify(itemService, times(1))
                .search(text, 0, 20);
    }

    @Test
    void createCommentTest() throws Exception {
        var commentDto = toCommentDto(comment);
        when(itemService.saveComment(1L, 1L, commentDto))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.authorName").value(commentDto.getAuthorName()));

        verify(itemService, times(1))
                .saveComment(item.getOwner().getId(), item.getId(), commentDto);
    }
}
