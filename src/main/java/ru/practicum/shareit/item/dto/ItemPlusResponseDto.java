package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ItemPlusResponseDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private LastBooking lastBooking;
    private NextBooking nextBooking;
    private List<Comment> comments;

    @Data
    @AllArgsConstructor
    public static class LastBooking {
        private Long id;
        private Long bookerId;
    }

    @Data
    @AllArgsConstructor
    public static class NextBooking {
        private Long id;
        private Long bookerId;
    }

    @Data
    @AllArgsConstructor
    public static class Comment {
        private Long id;
        private String text;
        private String authorName;
        private LocalDateTime created;
    }
}