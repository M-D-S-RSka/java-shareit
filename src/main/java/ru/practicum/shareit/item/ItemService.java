package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomExceptions.ItemException;
import ru.practicum.shareit.exceptions.CustomExceptions.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.setField;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemDto add(Long userId, ItemDto itemDto) {
        User user = findUser(userId);
        Item item = ItemMapper.itemModel(itemDto, user);
        log.info("add a new item: {}; for a user with id = {}", itemDto, userId);
        return ItemMapper.itemDto(itemStorage.add(item));
    }

    public ItemDto updateItem(Long userId, Long itemId, Map<String, Object> fields) {
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
            itemStorage.update(item);
            log.info("update the item with id = {} from the user with id = {}", itemId, userId);
            return ItemMapper.itemDto(item);
        } else {
            throw new UserNotFoundException(String.format("user %s is not the owner", user.getName()));
        }
    }

    public ItemDto findItemById(Long itemId) {
        log.info("find item with id = {}", itemId);
        return ItemMapper.itemDto(findItem(itemId));
    }

    public List<ItemDto> findAllByUserId(Long userId) {
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : itemStorage.findAll()) {
            if (item.getOwner().getId().equals(userId)) {
                itemsDto.add(ItemMapper.itemDto(item));
            }
        }
        log.info("found all user items = {} with id = {}", itemsDto.size(), userId);
        return itemsDto;
    }

    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            log.error("empty search request");
            return new ArrayList<>();
        }

        String modifiedText = text.replaceAll("\\s+", "").toLowerCase();
        return itemStorage.findAll().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().replaceAll("\\s+", "").toLowerCase().contains(modifiedText) ||
                                item.getDescription().replaceAll("\\s+", "").toLowerCase()
                                        .contains(modifiedText)))
                .map(ItemMapper::itemDto)
                .collect(Collectors.toList());
    }

    private User findUser(Long userId) {
        User user = userStorage.findById(userId);
        if (user != null) {
            log.info("find a user with id = {}", userId);
            return user;
        } else {
            throw new UserNotFoundException("user not found");
        }
    }

    private Item findItem(Long itemId) {
        Item item = itemStorage.findById(itemId);
        if (item != null) {
            log.info("find a item with id = {}", itemId);
            return item;
        } else {
            throw new ItemException("item not found");
        }
    }
}