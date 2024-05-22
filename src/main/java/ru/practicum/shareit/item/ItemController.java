package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPlusResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@RequestHeader(USER_ID_HEADER) Long userId, @RequestBody @Validated ItemDto itemDto) {
        log.info("Adding new item for user id: {}", userId);
        return itemService.add(userId, itemDto);
    }

    @PatchMapping("/{id}")
    public ItemDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long id,
                              @RequestBody @Validated Map<String, Object> fields) {
        log.info("Updating item id {} for user id {}", id, userId);
        return itemService.updateItem(userId, id, fields);
    }

    @GetMapping("/{id}")
    public ItemPlusResponseDto findItemById(@PathVariable Long id, @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Finding item by id {}", id);
        return itemService.findItemById(id, userId);
    }

    @GetMapping
    public List<ItemPlusResponseDto> findAllByUserId(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Finding all items by user id: {}", userId);
        return itemService.findAllByUserId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info("Searching items by text: {}", text);
        if (text == null || text.trim().isEmpty()) {
            log.info("Search text is empty, returning empty list.");
            return new ArrayList<>();
        }
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@PathVariable Long itemId, @RequestHeader(USER_ID_HEADER) Long userId,
                                         @RequestBody @Validated CommentRequestDto commentRequestDto) {
        log.info("Adding new commentRequestDto={} for userId={} by itemId={}", commentRequestDto, userId, itemId);
        return itemService.addComment(itemId, userId, commentRequestDto);
    }
}