package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.markers.Markers;

import javax.validation.constraints.Min;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.utils.UtilsClass.getPageable;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {
    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto add(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @RequestBody @Validated BookingRequestDto bookingRequestDto) {
        log.info("Adding new bookingRequestDto={} for user id: {}", bookingRequestDto, userId);
        return bookingService.add(userId, bookingRequestDto);
    }

    @PatchMapping(path = "/{bookingId}")
    public BookingResponseDto approveBooking(@PathVariable Long bookingId, @RequestParam Boolean approved,
                                             @RequestHeader(USER_ID_HEADER) Long ownerId) {
        log.info("BookingId={} approved={} ownerId={}", bookingId, approved, ownerId);
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping(path = "/{bookingId}")
    public BookingResponseDto getBookingByIdForOwnerOrBooker(@PathVariable Long bookingId,
                                                             @RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Booking bookingId={} received userId={}", bookingId, userId);
        return bookingService.getBookingByIdForOwnerOrBooker(bookingId, userId);
    }

//    @GetMapping
//    public List<BookingResponseDto> getAllBookingsForBooker(@RequestHeader(USER_ID_HEADER) Long userId,
//                                                            @RequestParam(required = false, defaultValue = "ALL")
//                                                            String state) {
//        log.info("All Bookings For Booker userId={}, state={}", userId, state);
//        return bookingService.getAllBookingsForOwnerOrBooker(userId, state, "BOOKER");
//    }

    @GetMapping
    public List<BookingResponseDto> getAllBookingsForBooker(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info("GET /bookings: userId={}, state={}, from={}, size={}", userId, state, from, size);

        Pageable pageable = getPageable(from, size);
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());

            return bookingService.getAllBookingsForBooker(userId, bookingState, pageable);
        } catch (IllegalArgumentException e) {
            throw new CustomExceptions.BookingStateException(String.format("Unknown state: %s", state));
        }
    }

//    @GetMapping(path = "/owner")
//    public List<BookingResponseDto> getAllBookingsForOwner(@RequestHeader(USER_ID_HEADER) Long userId,
//                                                           @RequestParam(required = false, defaultValue = "ALL")
//                                                           String state) {
//        log.info("All Bookings For Owner userId={}, state={}", userId, state);
//        return bookingService.getAllBookingsForOwnerOrBooker(userId, state, "OWNER");
//    }
//  СВЕРХУ МОЕ
    @GetMapping(path = "/owner")
    public List<BookingResponseDto> getAllBookingsForOwner(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(name = "state", defaultValue = "ALL") String state,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) Integer size) {
        log.info(
                "GET /bookings/owner: userId={}, state={}, from={}, size={}", userId, state, from, size);

        Pageable pageable = getPageable(from, size);
        try {
            BookingState bookingState = BookingState.valueOf(state.toUpperCase());

            return bookingService.getAllBookingsForOwner(userId, bookingState, pageable);
        } catch (IllegalArgumentException e) {
            throw new CustomExceptions.BookingStateException(String.format("Unknown state: %s", state));
        }
    }
}