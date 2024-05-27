package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto add(UserDto userDto) {
        try {
            User user = userRepository.save(UserMapper.userModel(userDto));
            log.info("created a new user {}", userDto);
            return UserMapper.userDto(user);
        } catch (DataIntegrityViolationException e) {
            throw new CustomExceptions.UserException("this email is already taken");
        }
    }

    public UserDto update(UserDto userRequest, Long id) {
        User user = getUserById(id);
        String name = userRequest.getName();
        String email = userRequest.getEmail();

        if (name != null) {
            user.setName(name);
        }
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new CustomExceptions.UserException("this email is already taken");
            }
            user.setEmail(email);
        }

        userRepository.save(user);
        log.info("updated the user with the id = {}", id);
        return UserMapper.userDto(user);
    }

    public UserDto findById(Long id) {
        log.info("found user with id = {}", id);
        return UserMapper.userDto(getUserById(id));
    }

    public void removeById(Long id) {
        log.info("deleting a user with an id = {}", id);
        userRepository.deleteById(id);
    }

    public List<UserDto> findAll() {
        log.info("All users found");
        List<UserDto> list = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserDto dto = UserMapper.userDto(user);
            list.add(dto);
        }
        return list;
    }

    private User getUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("user not found"));
    }
}