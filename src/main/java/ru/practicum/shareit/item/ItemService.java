package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPlusResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.setField;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    public ItemDto add(Long userId, ItemDto itemDto) {
        User user = findUser(userId);
        Item item = ItemMapper.toModel(itemDto, user);

        log.info("add a new item: {}; for a user with id = {}", itemDto, userId);
        return ItemMapper.toDto(itemRepository.save(item));
    }

    public ItemDto updateItem(Long userId, Long itemId, Map<String, Object> fields) {
        log.info("Let's update a thing with id = {} from a user with id = {}", userId, itemId);
        User user = findUser(userId);
        Item item = findItem(itemId);

        if (user.equals(item.getOwner())) {
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Field field = findField(Item.class, key);
                if (field != null) {
                    field.setAccessible(true);
                    if (value instanceof Integer) {
                        setField(field, item, ((Integer) value).longValue());
                    } else {
                        setField(field, item, value);
                    }
                }
            }
            itemRepository.save(item);

            log.info("update the item with id = {} from the user with id = {}", itemId, userId);
            return ItemMapper.toDto(item);
        } else {
            throw new CustomExceptions.UserNotFoundException(String.format("user %s is not the owner", user.getName()));
        }
    }

    public ItemPlusResponseDto findItemById(Long itemId, Long userId) {
        log.info("find item with id = {}", itemId);
        Booking lastBooking = bookingRepository.findLastBookingBeforeNow(itemId, userId);
        Booking nextBooking = bookingRepository.findNextBookingAfterNow(itemId, userId);
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return ItemMapper.toResponsePlusDto(findItem(itemId), lastBooking, nextBooking, comments);
    }

    public List<ItemPlusResponseDto> findAllByUserId(Long userId) {
        List<ItemPlusResponseDto> itemsDto = new ArrayList<>();
        for (Item item : itemRepository.findAll()) {
            if (item.getOwner().getId().equals(userId)) {
                Booking next = bookingRepository.findNextBookingAfterNow(item.getId(), userId);
                Booking last = bookingRepository.findLastBookingBeforeNow(item.getId(), userId);
                List<Comment> comments = commentRepository.findAllByItemId(item.getId());
                itemsDto.add(ItemMapper.toResponsePlusDto(item, last, next, comments));
            }
        }

        Comparator<ItemPlusResponseDto> comparator = Comparator.comparing(ItemPlusResponseDto::getId);
        itemsDto.sort(comparator);

        log.info("found all user items = {} with id = {}", itemsDto.size(), userId);
        return itemsDto;
    }

    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.error("empty search request");
            return new ArrayList<>();
        }
        return itemRepository.search(text).stream().map(ItemMapper::toDto).collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        log.info("find a user with id = {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("user not found"));
    }

    private Item findItem(Long itemId) {
        log.info("find a item with id = {}", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new CustomExceptions.ItemNotFoundException("Item not found"));
    }

    public CommentResponseDto addComment(Long itemId, Long userId, CommentRequestDto commentRequestDto) {
        Item item = itemRepository.findByIdAndBookerIdAndFinishedBooking(userId, itemId).orElseThrow(
                () -> new CustomExceptions.ItemNotAvailableException("The item is not available for comment"));
        User user = findUser(userId);

        if (commentRequestDto.getText() != null && !commentRequestDto.getText().isEmpty()) {
            Comment comment = CommentMapper.toModel(commentRequestDto, user, item);
            commentRepository.save(comment);

            log.info("User id={} added a comment to the id={} thing.", userId, itemId);
            return CommentMapper.toResponseDto(comment);
        } else {
            throw new CustomExceptions.UserException("The comment text cannot be empty");
        }
    }
}