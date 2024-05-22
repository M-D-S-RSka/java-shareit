package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    public BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto) {
        checkDateTime(bookingRequestDto.getStart(), bookingRequestDto.getEnd());

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new CustomExceptions.ItemNotFoundException("Item not found"));

        if (item.getAvailable()) {
            if (item.getOwner().getId().equals(userId))
                throw new CustomExceptions.ItemNotFoundException("The owner can't book his stuff");

            User user = getUser(userId);
            Booking booking = BookingMapper.toModel(bookingRequestDto, item, user, BookingStatus.WAITING);
            log.info("Add a new booking itemId = {}, userId = {}", bookingRequestDto.getItemId(), userId);
            return BookingMapper.toResponseDto(bookingRepository.save(booking));
        } else {
            throw new CustomExceptions.ItemNotAvailableException("Item is not available for booking");
        }
    }

    private User getUser(Long userId) {
        log.info("Get user with id {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomExceptions.UserNotFoundException("User not found"));
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new CustomExceptions.BookingDateTimeException("End date and time earlier than start date and time");
        } else if (start.equals(end)) {
            throw new CustomExceptions.BookingDateTimeException("The end and start dates and times are the same");
        }
    }

    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findBookingByIdAndOwnerId(ownerId, bookingId)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException("Requests not found"));

        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            throw new CustomExceptions.BookingStatusException("Status is now APPROVED");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    public BookingResponseDto getBookingByIdForOwnerOrBooker(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findBookingByIdAndOwnerIdOrBookerId(bookingId, userId)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException("Requests not found"));
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    public List<BookingResponseDto> getAllBookingsForOwnerOrBooker(Long userId, String bookingState, String userType) {
        List<Booking> bookings;
        try {
            BookingState state = BookingState.valueOf(bookingState);
            bookings = userType.equals("OWNER") ? bookingRepository
                    .findAllByOwnerId(userId) : bookingRepository.findAllByBookerId(userId);
            if (bookings.isEmpty()) {
                throw new CustomExceptions.BookingNotFoundException("Requests not found");
            }

            List<BookingResponseDto> bookingResponseDtos = new ArrayList<>();

            for (Booking booking : bookings) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime start = booking.getStart();
                LocalDateTime end = booking.getEnd();
                BookingStatus status = booking.getStatus();

                if (state == BookingState.ALL
                        || state == BookingState.CURRENT && now.isAfter(start) && now.isBefore(end)
                        || state == BookingState.FUTURE && now.isBefore(start)
                        || state == BookingState.PAST && now.isAfter(end)
                        || state == BookingState.WAITING && status.equals(BookingStatus.WAITING)
                        || state == BookingState.REJECTED && status.equals(BookingStatus.REJECTED)) {
                    bookingResponseDtos.add(BookingMapper.toResponseDto(booking));
                }
            }

            Comparator<BookingResponseDto> comparator =
                    Comparator.comparing(BookingResponseDto::getEnd, Comparator.reverseOrder());
            bookingResponseDtos.sort(comparator);

            return bookingResponseDtos;

        } catch (IllegalArgumentException e) {
            throw new CustomExceptions.BookingStateException(
                    String.format("Unknown state: %s", bookingState));
        }
    }
}