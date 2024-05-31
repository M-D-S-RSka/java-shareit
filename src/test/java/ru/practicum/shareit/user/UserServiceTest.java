package ru.practicum.shareit.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    public void setUp() {
        user = User.builder().id(1L).name("Timmy").email("timmy@email.com").build();
        userDto = UserDto.builder().name("Timmy").email("timmy@email.com").build();
    }

    @Test
    void create() {
        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

        UserDto savedUser = userService.add(userDto);

        Assertions.assertThat(savedUser).isNotNull();
    }

    @Test
    void update() {
        UserDto userUpdateDto = UserDto.builder().name("Timmy2").email("timmy2@email.com").build();

        when(userRepository.save(Mockito.any(User.class))).thenReturn(user);
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(user));

        UserDto updatedUser = userService.update(userUpdateDto, 1L);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getId()).isEqualTo(1L);
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Timmy2");
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("timmy2@email.com");

        userUpdateDto.setName(null);
        userUpdateDto.setEmail(null);

        UserDto updatedUserWithoutNameAndEmail = userService.update(userUpdateDto, 1L);

        Assertions.assertThat(updatedUserWithoutNameAndEmail).isNotNull();
        Assertions.assertThat(updatedUserWithoutNameAndEmail.getId()).isEqualTo(1L);
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Timmy2");
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("timmy2@email.com");
    }

    @Test
    void findById() {
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.ofNullable(user));

        UserDto updatedUser = userService.findById(1L);

        Assertions.assertThat(updatedUser).isNotNull();
        Assertions.assertThat(updatedUser.getId()).isEqualTo(1L);
        Assertions.assertThat(updatedUser.getName()).isEqualTo("Timmy");
        Assertions.assertThat(updatedUser.getEmail()).isEqualTo("timmy@email.com");
    }

    @Test
    void removeById() {
        userService.removeById(1L);

        verify(userRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void findAll() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        List<UserDto> userDtos = userService.findAll();

        Assertions.assertThat(userDtos.size()).isEqualTo(1);
        Assertions.assertThat(userDtos.get(0).getId()).isEqualTo(1L);
    }

    @Test
    public void findByIdShouldReturnException() {
        when(userRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(CustomExceptions.UserNotFoundException.class)
                .isThrownBy(() -> userService.findById(1L));
    }
}