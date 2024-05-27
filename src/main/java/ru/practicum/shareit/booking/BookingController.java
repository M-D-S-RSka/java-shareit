package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
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

    @GetMapping
    public List<BookingResponseDto> getAllBookingsForBooker(@RequestHeader(USER_ID_HEADER) Long userId,
                                                            @RequestParam(required = false, defaultValue = "ALL")
                                                            String state) {
        log.info("All Bookings For Booker userId={}, state={}", userId, state);
        return bookingService.getAllBookingsForOwnerOrBooker(userId, state, "BOOKER");
    }

    @GetMapping(path = "/owner")
    public List<BookingResponseDto> getAllBookingsForOwner(@RequestHeader(USER_ID_HEADER) Long userId,
                                                           @RequestParam(required = false, defaultValue = "ALL")
                                                           String state) {
        log.info("All Bookings For Owner userId={}, state={}", userId, state);
        return bookingService.getAllBookingsForOwnerOrBooker(userId, state, "OWNER");
    }
}