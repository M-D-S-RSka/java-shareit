package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {

    Item add(Item item);

    Item update(Item item);

    void remove(Long id);

    Item findById(Long id);

    List<Item> findAll();
}
