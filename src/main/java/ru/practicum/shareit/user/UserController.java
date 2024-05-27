package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.markers.Markers;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserDto add(@Validated(Markers.Create.class) @RequestBody UserDto userDto) {
        log.info("Adding new user");
        return userService.add(userDto);
    }

    @PatchMapping("/{id}")
    public UserDto update(@Validated @RequestBody UserDto userDto, @PathVariable Long id) {
        log.info("Updating user with id: {}", id);
        return userService.update(userDto, id);
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Long id) {
        log.info("Finding user by id: {}", id);
        return userService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable Long id) {
        log.info("Removing user by id: {}", id);
        userService.removeById(id);
    }

    @GetMapping
    public List<UserDto> findAll() {
        log.info("Finding all users");
        return userService.findAll();
    }
}