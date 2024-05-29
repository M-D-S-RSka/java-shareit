package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
@WebMvcTest(ItemRequestController.class)
public class ItemRequestControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean ItemRequestService itemRequestService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String USER_ID_HEADER = "X-Sharer-User-Id";
  private final LocalDateTime dateTime = LocalDateTime.of(2020, 1, 1, 1, 1, 1);

  private ItemRequestDto itemRequestDtoIn;
  private ItemRequestDto itemRequestDtoOut;
  private ItemDto itemDto;

  @BeforeEach
  public void setUp() {
    itemDto =
        ItemDto.builder()
            .id(1L)
            .name("Item1")
            .description("Item1 description")
            .available(true)
            .requestId(1L)
            .build();
    itemRequestDtoIn = ItemRequestDto.builder().description("description").build();
    itemRequestDtoOut =
        ItemRequestDto.builder()
            .id(1L)
            .description("description")
            .created(dateTime)
            .items(Collections.singletonList(itemDto))
            .build();
  }

  @Test
  public void addRequestShouldReturnItemRequestDto() throws Exception {
    when(itemRequestService.addRequest(any(ItemRequestDto.class), anyLong()))
        .thenReturn(itemRequestDtoOut);

    mockMvc
        .perform(
            post("/requests/")
                .header(USER_ID_HEADER, 1)
                .content(objectMapper.writeValueAsString(itemRequestDtoIn))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.description").value("description"));
  }

  @Test
  public void findByRequestIdShouldReturnItemRequestDto() throws Exception {
    when(itemRequestService.findByRequestId(anyLong(), anyLong())).thenReturn(itemRequestDtoOut);

    mockMvc
        .perform(get("/requests/{requestId}", 1).header(USER_ID_HEADER, 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.description").value("description"))
        .andExpect(jsonPath("$.created").value(dateTime.toString()));
  }

  @Test
  public void findAllByOwnerRequestIdShouldReturnListOfItemRequestDto() throws Exception {
    when(itemRequestService.findAllByOwnerRequestId(anyLong()))
        .thenReturn(Collections.singletonList(itemRequestDtoOut));

    mockMvc
        .perform(get("/requests").header(USER_ID_HEADER, 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  public void findAllShouldReturnListOfItemRequestDto() throws Exception {
    when(itemRequestService.findAll(anyLong(), any(Pageable.class)))
        .thenReturn(Collections.singletonList(itemRequestDtoOut));

    mockMvc
        .perform(
            get("/requests/all")
                .header(USER_ID_HEADER, 1)
                .queryParam("from", "1")
                .queryParam("size", "1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    mockMvc
        .perform(get("/requests/all").header(USER_ID_HEADER, 1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }
}
