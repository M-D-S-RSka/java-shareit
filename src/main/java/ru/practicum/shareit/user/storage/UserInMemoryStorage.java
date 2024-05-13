package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserInMemoryStorage implements UserStorage {

    private final Map<Long, User> userMap = new HashMap<>();

    private Long id = 1L;

    private Long getNextId() {
        return id++;
    }

    @Override
    public User add(User user) {
        user.setId(getNextId());
        userMap.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        return userMap.put(user.getId(), user);
    }

    @Override
    public void remove(Long id) {
        userMap.remove(id);
    }

    @Override
    public User findById(Long id) {
        return userMap.get(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userMap.values());
    }
}