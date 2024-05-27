package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

@Slf4j
@ControllerAdvice
public class ErrorHandler {

    @ResponseBody
    @ExceptionHandler({Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ErrorResponse getRuntimeExceptionResponse(Exception e) {
        log.error("INTERNAL_SERVER_ERROR: {}", e.getMessage());
        if (e.getClass().equals(CustomExceptions.BookingStateException.class)) {
            return new ErrorResponse(e.getMessage(), e.getMessage());
        }
        return new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({CustomExceptions.UserNotFoundException.class, CustomExceptions.ItemNotFoundException.class,
            CustomExceptions.BookingNotFoundException.class
    })
    ErrorResponse getUserNotFoundExceptionResponse(RuntimeException e) {
        log.error("NOT_FOUND: {}", e.getMessage());
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    @ResponseBody
    @ExceptionHandler({CustomExceptions.EmailException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, DataIntegrityViolationException.class,
            CustomExceptions.ItemNotAvailableException.class, CustomExceptions.BookingDateTimeException.class,
            CustomExceptions.BookingStatusException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ErrorResponse getEmailExceptionResponse(Exception e) {
        log.error("BAD_REQUEST: {}", e.getMessage());
        return new ErrorResponse("BAD_REQUEST", e.getMessage());
    }
}