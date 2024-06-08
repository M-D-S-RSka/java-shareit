package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.utils.UtilsClass.getPageable;

@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private BookingService bookingService;

    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime start = LocalDateTime.of(2025, 1, 1, 1, 1, 1);
    private final LocalDateTime end = LocalDateTime.of(2025, 1, 1, 2, 1, 1);

    private User user;
    private User otherUser;
    private ItemRequest itemRequest;
    private Item item;
    private Booking booking;
    private BookingRequestDto bookingRequestDto;
    private Pageable pageable;

    @BeforeEach
    public void setUp() {
        pageable = getPageable(0, 10);
        user = User.builder().id(1L).name("Timmy").email("timmy@email.com").build();
        otherUser = User.builder().id(2L).name("Gimmy").email("gimmy@email.com").build();
        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Item request description")
                .requester(user)
                .created(now)
                .build();
        item = Item.builder()
                .id(1L)
                .name("Item name")
                .description("Item description")
                .available(true)
                .owner(user)
                .build();
        booking = Booking.builder()
                .id(1L)
                .booker(user)
                .status(BookingStatus.WAITING)
                .item(item)
                .start(start)
                .end(end)
                .build();
        bookingRequestDto = BookingRequestDto.builder().itemId(1L).start(start).end(end).build();
    }

    @Test
    public void createShouldReturnBookingResponseDto() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(otherUser));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        long userId = 2L;
        BookingResponseDto createdBookingResponseDto = bookingService.add(userId, bookingRequestDto);

        assertThat(createdBookingResponseDto).isNotNull();
        assertThat(createdBookingResponseDto.getId()).isEqualTo(1);
        assertThat(createdBookingResponseDto.getStart()).isEqualTo(start);
    }

    @Test
    public void createShouldReturnItemNotFoundExceptionWrongBooking() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        long userId = 1L;

        assertThatExceptionOfType(CustomExceptions.ItemNotFoundException.class)
                .isThrownBy(() -> bookingService.add(userId, bookingRequestDto));
    }

    @Test
    public void createShouldReturnItemNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        long userId = 1L;

        assertThatExceptionOfType(CustomExceptions.ItemNotFoundException.class)
                .isThrownBy(() -> bookingService.add(userId, bookingRequestDto));
    }

    @Test
    public void createShouldReturnItemNotAvailableException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));

        item.setAvailable(false);
        long userId = 1L;

        assertThatExceptionOfType(CustomExceptions.ItemNotAvailableException.class)
                .isThrownBy(() -> bookingService.add(userId, bookingRequestDto));
    }

    @Test
    public void createShouldReturnUserNotFoundException() {
        when(itemRepository.findById(anyLong())).thenReturn(Optional.ofNullable(item));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        long userId = 2L;

        assertThatExceptionOfType(CustomExceptions.UserNotFoundException.class)
                .isThrownBy(() -> bookingService.add(userId, bookingRequestDto));
    }

    @Test
    public void approveBookingShouldReturnBookingNotFoundException() {
        when(bookingRepository.findByItemOwner_IdAndId(anyLong(), anyLong())).thenReturn(Optional.empty());

        long bookingId = 1;
        long userId = 1;
        boolean status = true;

        assertThatExceptionOfType(CustomExceptions.BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.approveBooking(bookingId, status, userId));
    }

    @Test
    public void approveBookingShouldReturnBookingStatusException() {
        when(bookingRepository.findByItemOwner_IdAndId(anyLong(), anyLong())).thenReturn(Optional.ofNullable(booking));

        long bookingId = 1;
        long userId = 1;
        boolean status = true;

        booking.setStatus(BookingStatus.APPROVED);

        assertThatExceptionOfType(CustomExceptions.BookingStatusException.class)
                .isThrownBy(() -> bookingService.approveBooking(bookingId, status, userId));
    }

    @Test
    public void approveBookingShouldReturnBookingResponseDto() {
        when(bookingRepository.findByItemOwner_IdAndId(anyLong(), anyLong())).thenReturn(Optional.ofNullable(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        long bookingId = 1;
        long userId = 1;
        boolean status = true;

        BookingResponseDto approvedBookingResponseDto = bookingService.approveBooking(bookingId, status, userId);

        assertThat(approvedBookingResponseDto).isNotNull();
        assertThat(approvedBookingResponseDto.getStatus()).isEqualTo(BookingStatus.APPROVED.toString());
    }

    @Test
    public void getBookingByIdForOwnerOrBookerShouldReturnBookingNotFoundException() {
        when(bookingRepository.findAllByItemOwner_IdOrderByStartDesc(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        long bookingId = 1;
        long userId = 1;

        assertThatExceptionOfType(CustomExceptions.BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.getBookingByIdForOwnerOrBooker(bookingId, userId));
    }

    @Test
    public void getBookingByIdForOwnerOrBookerShouldReturnBookingResponseDto() {
        when(bookingRepository.findAllByItemOwner_IdOrderByStartDesc(anyLong(), anyLong()))
                .thenReturn(Optional.ofNullable(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        long bookingId = 1;
        long userId = 1;

        BookingResponseDto foundBookingResponseDto = bookingService.getBookingByIdForOwnerOrBooker(bookingId, userId);

        assertThat(foundBookingResponseDto).isNotNull();
        assertThat(foundBookingResponseDto.getId()).isEqualTo(bookingId);
        assertThat(foundBookingResponseDto.getBooker().getId()).isEqualTo(userId);
    }

    @Test
    public void getAllBookingsForBookerStateAll() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.ALL, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForOwnerStateAll() {
        when(bookingRepository.findAllByItemOwner_IdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForOwner(1L, BookingState.ALL, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForBookerShouldReturnException() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        assertThatExceptionOfType(CustomExceptions.BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.getAllBookingsForBooker(1L, BookingState.ALL, pageable));
    }

    @Test
    public void getAllBookingsForOwnerShouldReturnException() {
        when(bookingRepository.findAllByItemOwner_IdOrderByStartDesc(anyLong(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        assertThatExceptionOfType(CustomExceptions.BookingNotFoundException.class)
                .isThrownBy(() -> bookingService.getAllBookingsForOwner(1L, BookingState.ALL, pageable));
    }

    @Test
    public void getAllBookingsForBookerStateCurrent() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        booking.setStart(LocalDateTime.of(2020, 1, 1, 1, 1, 1));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.CURRENT, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForBookerStatePast() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        booking.setStart(LocalDateTime.of(2020, 1, 1, 1, 1, 1));
        booking.setEnd(LocalDateTime.of(2020, 1, 2, 1, 1, 1));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.PAST, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForBookerStateFuture() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.FUTURE, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForBookerStateRejected() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        booking.setStatus(BookingStatus.REJECTED);

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.REJECTED, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }

    @Test
    public void getAllBookingsForBookerStateWaiting() {
        when(bookingRepository.findByBookerOrderByStartDesc(any(User.class), any(Pageable.class)))
                .thenReturn(Collections.singletonList(booking));
        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        List<BookingResponseDto> bookingResponseDtos =
                bookingService.getAllBookingsForBooker(1L, BookingState.WAITING, pageable);

        assertThat(bookingResponseDtos.size()).isGreaterThan(0);
    }
}