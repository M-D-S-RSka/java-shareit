package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ItemInMemoryStorage implements ItemStorage {

    private final Map<Long, Item> itemMap = new HashMap<>();

    private Long id = 1L;

    private Long getNextId() {
        return id++;
    }

    @Override
    public Item add(Item item) {
        item.setId(getNextId());
        itemMap.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        return itemMap.put(item.getId(), item);
    }

    @Override
    public void remove(Long id) {
        itemMap.remove(id);
    }

    @Override
    public Item findById(Long id) {
        return itemMap.get(id);
    }

    @Override
    public List<Item> findAll() {
        return new ArrayList<>(itemMap.values());
    }
}
