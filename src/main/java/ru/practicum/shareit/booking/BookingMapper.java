package ru.practicum.shareit.booking;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class BookingMapper {

    public static Booking toModel(BookingRequestDto bookingRequestDto, Item item, User user, BookingStatus status) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(status)
                .item(item)
                .booker(user)
                .build();
    }

    public static BookingResponseDto toResponseDto(Booking booking) {
        BookingResponseDto.Booker booker = new BookingResponseDto.Booker(booking.getBooker().getId());
        BookingResponseDto.Item item =
                new BookingResponseDto.Item(booking.getItem().getId(), booking.getItem().getName());

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus().toString())
                .booker(booker)
                .item(item)
                .build();
    }
}