package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

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
        log.info("Updating item id: {} for user id: {}", id, userId);
        return itemService.updateItem(userId, id, fields);
    }

    @GetMapping("/{id}")
    public ItemDto findItemById(@PathVariable Long id) {
        log.info("Finding item by id: {}", id);
        return itemService.findItemById(id);
    }

    @GetMapping
    public List<ItemDto> findAllByUserId(@RequestHeader(USER_ID_HEADER) Long userId) {
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
}