package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull
    @FutureOrPresent(message = "Start date and time in the past")
    private LocalDateTime start;

    @NotNull
    @FutureOrPresent(message = "End date and time in the past")
    private LocalDateTime end;

    private Long itemId;
}