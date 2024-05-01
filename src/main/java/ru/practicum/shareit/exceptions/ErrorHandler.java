package ru.practicum.shareit.exceptions;

import lombok.extern.slf4j.Slf4j;
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})
    ErrorResponse getRuntimeExceptionResponse(Exception e) {
        log.error("INTERNAL_SERVER_ERROR: {}", e.getMessage());
        return new ErrorResponse("INTERNAL_SERVER_ERROR", e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({CustomExceptions.UserNotFoundException.class})
    ErrorResponse getUserNotFoundExceptionResponse(RuntimeException e) {
        log.error("NOT_FOUND: {}", e.getMessage());
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({CustomExceptions.EmailException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    ErrorResponse getEmailExceptionResponse(Exception e) {
        log.error("BAD_REQUEST: {}", e.getMessage());
        return new ErrorResponse("BAD_REQUEST", e.getMessage());
    }
}