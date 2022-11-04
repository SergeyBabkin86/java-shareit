package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private Item item;
    private User user1;
    private User user2;
    private ItemRequest itemRequest;

    @BeforeEach
    void beforeEach() {
        user1 = userRepository.save(new User(1L, "user1", "user1@mail.ru"));
        user2 = userRepository.save(new User(2L, "user2", "user2@mail.ru"));
        itemRequest = itemRequestRepository.save(new ItemRequest(1L, "itemRequest1",
                LocalDateTime.now(), user2));
        item = itemRepository.save(new Item(1L, "item1", "description1",
                true, user1, itemRequest));
    }

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
    }

    @Test
    void search() {
        final var text = item.getDescription().substring(0, 3);
        final var items = itemRepository.search(text, Pageable.unpaged());
        final var incorrectText = "incorrect";
        final var emptyItems = itemRepository.search(incorrectText, Pageable.unpaged());

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item, items.get(0));
        assertTrue(emptyItems.isEmpty());
    }

    @Test
    void findAllByItemRequestId() {
        final var items = itemRepository.findAllByItemRequestId(itemRequest.getId());

        assertSame(user2, itemRequest.getUser());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertSame(item, items.get(0));
    }

    @Test
    void findByOwnerId() {
        final var byOwner = itemRepository.findByOwnerId(user1.getId(), Pageable.unpaged());

        assertNotNull(byOwner);
        assertEquals(1, byOwner.size());
        assertSame(item, byOwner.get(0));
    }
}
