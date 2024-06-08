package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemPlusResponseDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    ItemService itemService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private ItemDto itemDtoIn;
    private ItemDto itemDtoOut;
    private ItemPlusResponseDto itemPlusResponseDto;
    private ItemPlusResponseDto.Comment itemComment;

    @BeforeEach
    public void setUp() {
        itemDtoOut = ItemDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        itemDtoIn = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .requestId(1L)
                .build();

        itemComment = new ItemPlusResponseDto.Comment(1L, "Comment", "AuthorName", LocalDateTime.now());

        itemPlusResponseDto = ItemPlusResponseDto.builder()
                .id(1L)
                .name("ItemPlus")
                .description("ItemPlus Description")
                .available(true)
                .lastBooking(new ItemPlusResponseDto.LastBooking(22L, 2L))
                .nextBooking(new ItemPlusResponseDto.NextBooking(33L, 3L))
                .comments(Collections.singletonList(itemComment))
                .build();
    }

    @Test
    public void createShouldReturnItemDto() throws Exception {
        when(itemService.add(anyLong(), any(ItemDto.class))).thenReturn(itemDtoOut);

        mockMvc.perform(post("/items")
                        .content(objectMapper.writeValueAsString(itemDtoIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDtoOut.getId()));
    }

    @Test
    public void updateFieldsShouldReturnUpdatedItemDto() throws Exception {
        when(itemService.updateItem(anyLong(), anyLong(), anyMap())).thenReturn(itemDtoOut);

        mockMvc.perform(patch("/items/{id}", 1)
                        .content(objectMapper.writeValueAsString(itemDtoIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemDtoOut.getId()));
    }

    @Test
    public void updateFieldsShouldReturnNotFound() throws Exception {
        doThrow(CustomExceptions.UserNotFoundException.class)
                .when(itemService)
                .updateItem(anyLong(), anyLong(), anyMap());

        mockMvc.perform(patch("/items/{id}", 1)
                        .content(objectMapper.writeValueAsString(itemDtoIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateFieldsShouldReturnEternalServerError() throws Exception {
        doThrow(RuntimeException.class).when(itemService).updateItem(anyLong(), anyLong(), anyMap());

        mockMvc.perform(patch("/items/{id}", 1)
                        .content(objectMapper.writeValueAsString(itemDtoIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    public void findItemByIdShouldReturnItemPlusResponseDto() throws Exception {
        when(itemService.findItemById(anyLong(), anyLong())).thenReturn(itemPlusResponseDto);

        mockMvc.perform(get("/items/{id}", 1).header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemPlusResponseDto.getId()))
                .andExpect(jsonPath("$.name").value(itemPlusResponseDto.getName()));
    }

    @Test
    public void findAllByUserIdShouldReturnListOfItemPlusResponseDto() throws Exception {
        when(itemService.findAllByUserId(anyLong(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(itemPlusResponseDto));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1)
                        .queryParam("from", "1").queryParam("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/items").header(USER_ID_HEADER, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void searchShouldReturnListOfItemDto() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", "text");
        params.add("from", "1");
        params.add("size", "2");
        when(itemService.search(anyString(), any(Pageable.class))).thenReturn(Collections.singletonList(itemDtoOut));

        mockMvc.perform(get("/items/search").queryParams(params))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/items/search").queryParam("text", "text"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    public void addCommentShouldReturnItemWithComment() throws Exception {
        CommentResponseDto commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Comment")
                .authorName("Author Name")
                .created(LocalDateTime.now())
                .build();
        CommentRequestDto commentRequestDto = new CommentRequestDto("Comment");

        when(itemService.addComment(anyLong(), anyLong(), any(CommentRequestDto.class)))
                .thenReturn(commentResponseDto);

        mockMvc.perform(post("/items/{itemId}/comment", 1)
                        .header(USER_ID_HEADER, 1)
                        .content(objectMapper.writeValueAsString(commentRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.authorName").value(commentResponseDto.getAuthorName()))
                .andExpect(jsonPath("$.text").value(commentResponseDto.getText()));
    }

    @Test
    public void searchShouldReturnEmptyList() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("text", "");
        params.add("from", "1");
        params.add("size", "2");
        when(itemService.search(anyString(), any(Pageable.class)))
                .thenReturn(Collections.singletonList(itemDtoOut));

        mockMvc.perform(get("/items/search").queryParams(params))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}