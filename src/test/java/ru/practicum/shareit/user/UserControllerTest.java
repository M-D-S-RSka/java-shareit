package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {
  @Autowired private MockMvc mockMvc;
  @MockBean UserService userService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private UserDto userRequestDto;
  private UserDto userResponseDto;

  @BeforeEach
  public void setUp() {
    userRequestDto = UserDto.builder().name("Timmy").email("timmy@email.com").build();
    userResponseDto = UserDto.builder().id(1L).name("Timmy").email("timmy@email.com").build();
  }

  @Test
  public void createShouldReturnUserDto() throws Exception {
    when(userService.add(any(UserDto.class))).thenReturn(userResponseDto);

    mockMvc
        .perform(
            post("/users")
                .content(objectMapper.writeValueAsString(userRequestDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Timmy"))
        .andExpect(jsonPath("$.email").value("timmy@email.com"));
  }

  @Test
  public void updateShouldReturnUpdatedUserDto() throws Exception {
    UserDto updatedUserDto =
        UserDto.builder().id(1L).name("Timmy2").email("timmy2@email.com").build();

    when(userService.update(any(UserDto.class), anyLong())).thenReturn(updatedUserDto);

    mockMvc
        .perform(
            patch("/users/{id}", 1)
                .content(objectMapper.writeValueAsString(userRequestDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(updatedUserDto.getId()))
        .andExpect(jsonPath("$.name").value(updatedUserDto.getName()))
        .andExpect(jsonPath("$.email").value(updatedUserDto.getEmail()));
  }

  @Test
  void findByIdShouldReturnUserDto() throws Exception {
    when(userService.findById(anyLong())).thenReturn(userResponseDto);

    mockMvc
        .perform(get("/users/{id}", 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userResponseDto.getId()))
        .andExpect(jsonPath("$.name").value(userResponseDto.getName()))
        .andExpect(jsonPath("$.email").value(userResponseDto.getEmail()));
  }

  @Test
  void removeByIdShouldReturnStatusOk() throws Exception {
    doNothing().when(userService);
    mockMvc.perform(delete("/users/{id}", 1)).andExpect(status().isOk());
  }

  @Test
  void findAllReturnListOfUserDtos() throws Exception {
    when(userService.findAll()).thenReturn(Collections.singletonList(userResponseDto));

    mockMvc
        .perform(get("/users"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }
}
