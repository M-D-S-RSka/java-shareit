package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class BookingService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public BookingResponseDto add(Long userId, BookingRequestDto bookingRequestDto) {
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

    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findByItemOwner_IdAndId(ownerId, bookingId)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException("Requests not found"));

        if (booking.getStatus() == BookingStatus.APPROVED && approved) {
            throw new CustomExceptions.BookingStatusException("Status is now APPROVED");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponseDto getBookingByIdForOwnerOrBooker(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findAllByItemOwner_IdOrderByStartDesc(bookingId, userId)
                .orElseThrow(() -> new CustomExceptions.BookingNotFoundException("Requests not found"));
        return BookingMapper.toResponseDto(bookingRepository.save(booking));
    }

    private List<BookingResponseDto> getAllBookingsForOwnerOrBooker(List<Booking> bookings, BookingState state) {
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
    }

    public List<BookingResponseDto> getAllBookingsForBooker(Long userId, BookingState bookingState, Pageable pageable) {
        User user = getUser(userId);
        List<Booking> bookings = bookingRepository.findByBookerOrderByStartDesc(user, pageable);
        return getAllBookingsForOwnerOrBooker(bookings, bookingState);
    }

    public List<BookingResponseDto> getAllBookingsForOwner(Long userId, BookingState bookingState, Pageable pageable) {
        List<Booking> bookings = bookingRepository.findAllByItemOwner_IdOrderByStartDesc(userId, pageable);
        return getAllBookingsForOwnerOrBooker(bookings, bookingState);
    }
}