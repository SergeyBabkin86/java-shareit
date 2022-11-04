package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.model.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequestDtoWithItems;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequest;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.toItemRequestDto;
import static ru.practicum.shareit.utilities.Checker.checkRequestAvailability;
import static ru.practicum.shareit.utilities.Checker.checkUserAvailability;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper mapper;
    private final UserRepository userRepository;


    @Override
    public ItemRequestDto save(ItemRequestDto itemRequestDto, Long userId) {
        checkUserAvailability(userId, userRepository);
        var itemRequest = toItemRequest(itemRequestDto);
        itemRequest.setUser(userRepository.findById(userId).get());
        return toItemRequestDto(itemRequestRepository.save(itemRequest));
    }

    @Override
    public List<ItemRequestDtoWithItems> findAll(Long userId) {
        checkUserAvailability(userId, userRepository);
        return itemRequestRepository.findAllByUserIdOrderByCreatedDesc(userId)
                .stream()
                .map(mapper::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDtoWithItems> findAllRequests(Long userId, int from, int size) {
        checkUserAvailability(userId, userRepository);

        if (from < 0 || size <= 0) {
            throw new ValidationException("Переданы некорректные значения from and size");
        }

        var page = from / size;
        var pageable = PageRequest.of(page, size, Sort.by("created"));

        return itemRequestRepository.findAll(pageable)
                .stream()
                .filter(itemRequest -> !itemRequest.getUser().getId().equals(userId))
                .map(mapper::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDtoWithItems findByRequestId(Long userId, Long requestId) {
        checkUserAvailability(userId, userRepository);
        checkRequestAvailability(requestId, itemRequestRepository);

        return mapper.toItemRequestDtoWithItems(itemRequestRepository.findById(requestId).get());
    }
}
