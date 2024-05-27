package ru.practicum.shareit.booking.customAnnotation;

import ru.practicum.shareit.booking.dto.BookingRequestDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DateTimeValidator implements ConstraintValidator<ValidDateTime, BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto bookingRequestDto, ConstraintValidatorContext context) {
        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            return true;
        }
        return bookingRequestDto.getEnd().isAfter(bookingRequestDto.getStart());
    }
}