package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.customAnnotation.ValidDateTime;
import ru.practicum.shareit.markers.Markers;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
@ValidDateTime()
@AllArgsConstructor
public class BookingRequestDto {
    @NotNull
    @FutureOrPresent(message = "Start date and time in the past")
    private LocalDateTime start;

    @NotNull
    @Future(message = "End date and time in the past or present")
    private LocalDateTime end;

    private Long itemId;
}