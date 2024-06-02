package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.ItemRequestMapper.toDto;
import static ru.practicum.shareit.request.ItemRequestMapper.toModel;

@Service
@RequiredArgsConstructor
public class ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    private User getUser(Long userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () ->
                                new CustomExceptions.UserNotFoundException(
                                        String.format("Пользователь с id = %s не найден", userId)));
    }

    @Transactional
    public ItemRequestDto addRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = getUser(userId);
        ItemRequest itemRequest = toModel(itemRequestDto, user);
        return toDto(itemRequestRepository.save(itemRequest));
    }

    public List<ItemRequestDto> findAllByOwnerRequestId(Long userId) {
        User user = getUser(userId);

        if (itemRequestRepository.findByRequester(user) == null || itemRequestRepository.findByRequester(user).isEmpty()) {
            return List.of();
        } else {
            return itemRequestRepository.findByRequester(user).stream()
                    .map(request -> toDto(request, getItems(request)))
                    .collect(Collectors.toList());
        }
    }

    public List<ItemRequestDto> findAll(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return itemRequestRepository.findAllByRequesterNot(user, pageable).stream()
                .map(request -> toDto(request, getItems(request)))
                .collect(Collectors.toList());
    }

    public ItemRequestDto findByRequestId(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest itemRequest = itemRequestRepository.findById(requestId).orElseThrow(() ->
                new CustomExceptions.ItemRequestNotFoundException(
                        String.format("Запрос с id=%s не найден", requestId)));

        ItemRequestDto itemRequestDto = toDto(itemRequest);
        itemRequestDto.setItems(getItems(itemRequest));

        return itemRequestDto;
    }

    private List<ItemDto> getItems(ItemRequest request) {
        return itemRepository.findByRequest(request).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}