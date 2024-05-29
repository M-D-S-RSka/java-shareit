package ru.practicum.shareit.request;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exceptions.CustomExceptions;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.utils.UtilsClass.getPageable;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private ItemRepository itemRepository;
  @Mock private ItemRequestRepository itemRequestRepository;

  @InjectMocks private ItemRequestService itemRequestService;

  private User user;
  private User otherUser;
  private ItemRequest itemRequest;
  private ItemRequest otherItemRequest;
  private ItemRequestDto itemRequestDtoOut;
  private ItemRequestDto itemRequestDtoIn;
  private ItemDto itemDto;
  private ItemDto otherItemDto;
  private Item item;
  private Item otherItem;
  private Pageable pageable;

  @BeforeEach
  public void setUp() {
    LocalDateTime dateTime = LocalDateTime.of(2020, 1, 1, 1, 1, 1);
    pageable = getPageable(0, 10);

    user = User.builder().id(1L).name("Timmy").email("timmy@email.com").build();
    otherUser = User.builder().id(2L).name("Gimmy").email("gimmy@email.com").build();
    itemDto =
        ItemDto.builder()
            .id(1L)
            .name("Item name")
            .description("Item description")
            .available(true)
            .requestId(1L)
            .build();
    otherItemDto = ItemDto.builder()
            .id(2L)
            .name("Item2 name")
            .description("Item2 description")
            .available(true)
            .requestId(2L)
            .build();
    itemRequest =
        ItemRequest.builder()
            .id(1L)
            .description("Request description")
            .requester(user)
            .created(dateTime)
            .build();
    otherItemRequest =
        ItemRequest.builder()
            .id(2L)
            .description("Request2 description")
            .requester(otherUser)
            .created(dateTime)
            .build();
    itemRequestDtoOut =
        ItemRequestDto.builder()
            .id(1L)
            .description("Request description")
            .created(dateTime)
            .items(Collections.singletonList(itemDto))
            .build();

    otherItem = Item.builder()
            .id(2L)
            .name("Item2 name")
            .description("Item2 description")
            .request(otherItemRequest)
            .available(true)
            .owner(otherUser)
            .build();

    item = Item.builder()
            .id(1L)
            .name("Item1 name")
            .description("Item1 description")
            .request(itemRequest)
            .available(true)
            .owner(user)
            .build();

    itemRequestDtoIn = ItemRequestDto.builder().description("Request description").build();
  }

  @Test
  public void addRequestShouldReturnItemRequestDto() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
    when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

    ItemRequestDto createdItemRequestDto = itemRequestService.addRequest(itemRequestDtoIn, 1L);

    assertThat(createdItemRequestDto).isNotNull();
    assertThat(createdItemRequestDto.getId()).isEqualTo(1);
  }

  @Test
  public void findAllByOwnerRequestIdShouldReturnListOfDto() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
    when(itemRequestRepository.findByRequester(any(User.class)))
        .thenReturn(Collections.singletonList(itemRequest));

    List<ItemRequestDto> foundItemRequestDtos = itemRequestService.findAllByOwnerRequestId(1L);

    assertThat(foundItemRequestDtos.size()).isEqualTo(1);
    assertThat(foundItemRequestDtos.get(0).getId()).isEqualTo(1);
  }

  @Test
  public void findAllShouldReturnListOfDto() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
    when(itemRequestRepository.findAllByRequesterNot(any(User.class), any(Pageable.class)))
        .thenReturn(Collections.singletonList(otherItemRequest));
    when(itemRepository.findByRequest(any(ItemRequest.class))).thenReturn(Collections.singletonList(otherItem));

    List<ItemRequestDto> foundItemRequestDtos = itemRequestService.findAll(1L, pageable);

    assertThat(foundItemRequestDtos.size()).isEqualTo(1);
    assertThat(foundItemRequestDtos.get(0).getId()).isEqualTo(2);
    assertThat(foundItemRequestDtos.get(0).getItems().get(0).getId()).isEqualTo(2);
  }

  @Test
  public void findByRequestIdShouldReturnDto() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
    when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.ofNullable(itemRequest));
    when(itemRepository.findByRequest(any(ItemRequest.class))).thenReturn(Collections.singletonList(item));

    ItemRequestDto foundItemRequestDto = itemRequestService.findByRequestId(1L, 1L);

    assertThat(foundItemRequestDto).isNotNull();
    assertThat(foundItemRequestDto.getId()).isEqualTo(1);
    assertThat(foundItemRequestDto.getItems().get(0).getId()).isEqualTo(1);
  }

  @Test
  public void addRequestShouldReturnUserNotFoundException() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    Assertions.assertThatExceptionOfType(CustomExceptions.UserNotFoundException.class)
            .isThrownBy(() -> itemRequestService.addRequest(itemRequestDtoIn, 1L));
  }

  @Test
  public void findByRequestIdShouldReturnItemRequestNotFoundException() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));
    when(itemRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

    Assertions.assertThatExceptionOfType(CustomExceptions.ItemRequestNotFoundException.class)
            .isThrownBy(() -> itemRequestService.findByRequestId(1L, 1L));
  }
}
