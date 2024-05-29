package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
public class BookingControllerTest {
  @Autowired MockMvc mockMvc;
  @MockBean BookingService bookingService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  private static final String USER_ID_HEADER = "X-Sharer-User-Id";

  private BookingRequestDto bookingRequestDto;
  private BookingResponseDto bookingResponseDto;
  private User user;
  private BookingResponseDto.Item bookingResponseDtoItem;
  private BookingResponseDto.Booker booker;

  @BeforeEach
  public void setUp() {
    final LocalDateTime start = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
    final LocalDateTime end = LocalDateTime.of(2025, 1, 1, 2, 1, 1);

    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    user = User.builder().id(1L).name("Timmy").email("timmy@email.com").build();
    bookingResponseDtoItem = new BookingResponseDto.Item(1L, "Item1 name");
    booker = new BookingResponseDto.Booker(1L);

    bookingRequestDto = BookingRequestDto.builder().itemId(1L).start(start).end(end).build();
    bookingResponseDto =
        BookingResponseDto.builder()
            .id(1L)
            .item(bookingResponseDtoItem)
            .booker(booker)
            .status(BookingStatus.WAITING.toString())
            .build();
  }

  @Test
  public void createShouldReturnBookingResponseDto() throws Exception {
    when(bookingService.add(anyLong(), any(BookingRequestDto.class)))
        .thenReturn(bookingResponseDto);

    mockMvc
        .perform(
            post("/bookings")
                .header(USER_ID_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value(BookingStatus.WAITING.toString()));
  }

  @Test
  public void createShouldReturnBadRequest() throws Exception {
    when(bookingService.add(anyLong(), any(BookingRequestDto.class)))
        .thenReturn(bookingResponseDto);

    bookingRequestDto.setStart(LocalDateTime.of(2020, 1, 1, 1, 1, 1));

    mockMvc
        .perform(
            post("/bookings")
                .header(USER_ID_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    bookingRequestDto.setStart(LocalDateTime.of(2025, 1, 1, 1, 1, 1));
    bookingRequestDto.setEnd(LocalDateTime.of(2020, 1, 1, 1, 1, 1));

    mockMvc
        .perform(
            post("/bookings")
                .header(USER_ID_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    bookingRequestDto.setStart(LocalDateTime.of(2025, 1, 1, 1, 1, 1));
    bookingRequestDto.setEnd(LocalDateTime.of(2025, 1, 1, 1, 1, 1));

    mockMvc
        .perform(
            post("/bookings")
                .header(USER_ID_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());

    bookingRequestDto.setStart(null);
    bookingRequestDto.setEnd(LocalDateTime.of(2025, 1, 1, 1, 1, 1));

    mockMvc
        .perform(
            post("/bookings")
                .header(USER_ID_HEADER, 1L)
                .content(objectMapper.writeValueAsString(bookingRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void approveBookingShouldReturnBookingResponseDto() throws Exception {
    when(bookingService.approveBooking(anyLong(), anyBoolean(), anyLong()))
        .thenReturn(bookingResponseDto);

    int bookingId = 1;
    int userId = 1;
    String approved = "true";
    bookingResponseDto.setStatus(BookingStatus.APPROVED.toString());

    mockMvc
        .perform(
            patch("/bookings/{bookingId}", bookingId)
                .header(USER_ID_HEADER, userId)
                .param("approved", approved))
        .andExpect(status().isOk())
        .andExpect(jsonPath("status").value(BookingStatus.APPROVED.toString()));
  }

  @Test
  public void getBookingByIdForOwnerOrBookerShouldReturnBookingResponseDto() throws Exception {
    when(bookingService.getBookingByIdForOwnerOrBooker(anyLong(), anyLong()))
        .thenReturn(bookingResponseDto);
    int bookingId = 1;
    int userId = 1;
    mockMvc
        .perform(get("/bookings/{bookingId}", bookingId).header(USER_ID_HEADER, userId))
        .andExpect(status().isOk());
  }

  @Test
  public void getAllBookingsForOwnerShouldReturnListOfDto() throws Exception {
    when(bookingService.getAllBookingsForOwner(
            anyLong(), any(BookingState.class), any(Pageable.class)))
        .thenReturn(Collections.singletonList(bookingResponseDto));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("state", "ALL");
    params.add("from", "1");
    params.add("size", "1");

    int userId = 1;
    mockMvc
        .perform(get("/bookings").header(USER_ID_HEADER, userId).params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void getAllBookingsForBookerShouldReturnListOfDto() throws Exception {
    when(bookingService.getAllBookingsForBooker(
            anyLong(), any(BookingState.class), any(Pageable.class)))
        .thenReturn(Collections.singletonList(bookingResponseDto));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("state", "ALL");
    params.add("from", "1");
    params.add("size", "1");

    int userId = 1;
    mockMvc
        .perform(get("/bookings/owner").header(USER_ID_HEADER, userId).params(params))
        .andExpect(status().isOk());
  }

  @Test
  public void getAllBookingsForOwnerShouldReturnException() throws Exception {
    when(bookingService.getAllBookingsForOwner(
            anyLong(), any(BookingState.class), any(Pageable.class)))
            .thenReturn(Collections.singletonList(bookingResponseDto));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("state", "UNKNOWN");
    params.add("from", "1");
    params.add("size", "1");

    int userId = 1;
    mockMvc
            .perform(get("/bookings").header(USER_ID_HEADER, userId).params(params))
            .andExpect(status().is5xxServerError());
  }

  @Test
  public void getAllBookingsForBookerShouldReturnException() throws Exception {
    when(bookingService.getAllBookingsForBooker(
            anyLong(), any(BookingState.class), any(Pageable.class)))
            .thenReturn(Collections.singletonList(bookingResponseDto));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("state", "UNKNOWN");
    params.add("from", "1");
    params.add("size", "1");

    int userId = 1;
    mockMvc
            .perform(get("/bookings/owner").header(USER_ID_HEADER, userId).params(params))
            .andExpect(status().is5xxServerError());
  }
}
