package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.CustomExceptions.EmailException;
import ru.practicum.shareit.exceptions.CustomExceptions.UserException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public UserDto add(UserDto userDto) {
        checkEmail(userDto.getEmail());
        User user = userStorage.add(UserMapper.userModel(userDto));
        log.info("created a new user {}", userDto);
        return UserMapper.userDto(user);
    }

    public UserDto update(UserDto userRequest, Long id) {
        User user = userStorage.findById(id);
        String name = userRequest.getName();
        String email = userRequest.getEmail();

        if (name != null) user.setName(name);
        if (email != null) {
            if (!email.equals(user.getEmail())) {
                checkEmail(email);
            }
            user.setEmail(email);
        }

        log.info("updated the user with the id = {}", id);
        return UserMapper.userDto(user);
    }

    public UserDto findById(Long id) {
        log.info("found user with id = {}", id);
        return UserMapper.userDto(userStorage.findById(id));
    }

    public void removeById(Long id) {
        log.info("deleting a user with an id = {}", id);
        userStorage.remove(id);
    }

    public List<UserDto> findAll() {
        log.info("All users found");
        List<UserDto> list = new ArrayList<>();
        for (User user : userStorage.findAll()) {
            UserDto dto = UserMapper.userDto(user);
            list.add(dto);
        }
        return list;
    }

    private void checkEmail(String newEmail) {
        if (newEmail == null) {
            throw new EmailException("no mail has been set");
        }

        Set<String> emails = new HashSet<>();
        for (User user : userStorage.findAll()) {
            String email = user.getEmail();
            emails.add(email);
        }

        int emailsSize = emails.size();
        emails.add(newEmail);
        int newEmailsSize = emails.size();

        if (emailsSize == newEmailsSize) {
            throw new UserException("this email is already taken.");
        }
        log.info("checking the email string: {}", newEmail);
    }
}