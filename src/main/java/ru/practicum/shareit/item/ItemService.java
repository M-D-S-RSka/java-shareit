package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.lang.reflect.Field;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.setField;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    public ItemDto add(Long userId, ItemDto itemDto) {
        User user = findUser(userId);
        Item item;

        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId()).orElse(null);
            item = ItemMapper.toModel(itemDto, user, itemRequest);
        } else {
            item = ItemMapper.toModel(itemDto, user);
        }
        Item itemToSave = itemRepository.save(item);
        log.info("add a new item: {}; for a user with id = {}", itemDto, userId);
        return ItemMapper.toDto(itemToSave);
    }

    @Transactional
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
        Booking lastBooking = bookingRepository
                .findFirstByItemIdAndItemOwner_IdAndStatusAndStartDateBeforeOrderByEndDateDesc(itemId, userId);
        Booking nextBooking = bookingRepository.findAllByBooker_IdOrderByStartDesc(itemId, userId);
        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        return ItemMapper.toResponsePlusDto(findItem(itemId), lastBooking, nextBooking, comments);
    }

    // TODO - убрать обращения к бд из цикла
    // к сожалению, в текущий момент нет возможности реализовать без обращения, т к требует много правок, не только в
    // репозиториях...
//    public List findAllByUserId(long userId, Pageable pageable) {
//        Page items = itemRepository.findByOwner_Id(userId, pageable);
//
//        Map<Item, List<Booking>> approvedBookings =
//                (Map<Item, List<Booking>>) bookingRepository.findApprovedForItems(items.getContent(),
//                Sort.by(DESC, "start"))
//                        .stream()
//                        .collect(Collectors.groupingBy(Booking::getItem, Collectors.toList()));
//        Map<Item, List<Comment>> comments = (Map<Item, List<Comment>>) commentRepository
//        .findByItemIn(items.getContent(), Sort.by(DESC, "created"))
//                .stream()
//                .collect(Collectors.groupingBy(Comment::getItem, Collectors.toList()));
//
//        List<ItemPlusResponseDto> results = new ArrayList<>();
//        for (Object item : items) {
//            ItemPlusResponseDto itemInfo = ItemMapper.toResponsePlusDto(
//                    item,
//                    approvedBookings.getOrDefault((Item) item, Collections.emptyList()),
//                    comments.getOrDefault((Item) item, Collections.emptyList())
//            );
//            results.add(itemInfo);
//        }
//        return results;
//    }

    public List<ItemPlusResponseDto> findAllByUserId(Long userId, Pageable pageable) {
        List<ItemPlusResponseDto> itemsDto = new ArrayList<>();
        User user = findUser(userId);
        itemRepository.findByOwner(user, pageable).forEach(item -> {
            Booking next = bookingRepository.findAllByBooker_IdOrderByStartDesc(item.getId(), userId);
            Booking last = bookingRepository
                    .findFirstByItemIdAndItemOwner_IdAndStatusAndStartDateBeforeOrderByEndDateDesc(item.getId(),
                            userId);
            List<Comment> comments = commentRepository.findAllByItemId(item.getId());
            itemsDto.add(ItemMapper.toResponsePlusDto(item, last, next, comments));
        });
        Comparator<ItemPlusResponseDto> comparator = Comparator.comparing(ItemPlusResponseDto::getId);
        itemsDto.sort(comparator);
        log.info("found all user items = {} with id = {}", itemsDto.size(), userId);
        return itemsDto;
    }

    public List<ItemDto> search(String text, Pageable pageable) {
        if (text == null || text.isBlank()) {
            log.error("empty search request");
            return new ArrayList<>();
        }

        List<Item> items = itemRepository.search(text, pageable);
        List<ItemDto> itemsDto = items.stream()
                .map(ItemMapper::toDto)
                .collect(toList());

        log.info("Total items found: {}", itemsDto.size());
        return itemsDto;
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

    @Transactional
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