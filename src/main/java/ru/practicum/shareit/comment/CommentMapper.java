package ru.practicum.shareit.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {
    public static Comment toModel(CommentRequestDto commentRequestDto, User user, Item item) {
        return Comment.builder()
                .text(commentRequestDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();
    }

    public static CommentResponseDto toResponseDto(Comment comment) {
        return CommentResponseDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
