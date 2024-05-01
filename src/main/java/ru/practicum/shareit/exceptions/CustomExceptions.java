package ru.practicum.shareit.exceptions;

public class CustomExceptions {

    public static class UserException extends RuntimeException {
        public UserException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends UserException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }

    public static class ItemException extends RuntimeException {
        public ItemException(String message) {
            super(message);
        }
    }

    public static class EmailException extends UserException {
        public EmailException(String message) {
            super(message);
        }
    }
}