package ru.practicum.shareit.item;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.comment.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPlusResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class ItemMapper {
    public static ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() == null ? null : item.getRequest().getId())
                .build();
    }

    public static Item toModel(ItemDto itemDto, User user,  ItemRequest itemRequest) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .request(itemRequest)
                .build();
    }

    public static Item toModel(ItemDto itemDto, User user) {
        return Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .owner(user)
                .build();
    }

    public static ItemPlusResponseDto toResponsePlusDto(Item item, Booking lastBooking, Booking nextBooking,
                                                        List<Comment> comments) {
        ItemPlusResponseDto.LastBooking last = null;
        ItemPlusResponseDto.NextBooking next = null;
        List<ItemPlusResponseDto.Comment> itemComments = new ArrayList<>();

        if (lastBooking != null) {
            last = new ItemPlusResponseDto.LastBooking(lastBooking.getId(), lastBooking.getBooker().getId());
        }

        if (nextBooking != null) {
            next = new ItemPlusResponseDto.NextBooking(nextBooking.getId(), nextBooking.getBooker().getId());
        }

        if (!comments.isEmpty()) {
            List<ItemPlusResponseDto.Comment> list = new ArrayList<>();
            for (Comment comment : comments) {
                ItemPlusResponseDto.Comment comment1 = new ItemPlusResponseDto.Comment(
                        comment.getId(),
                        comment.getText(),
                        comment.getAuthor().getName(),
                        comment.getCreated());
                list.add(comment1);
            }
            itemComments = list;
        }

        return ItemPlusResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .available(item.getAvailable())
                .description(item.getDescription())
                .lastBooking(last)
                .nextBooking(next)
                .comments(itemComments)
                .build();
    }
}