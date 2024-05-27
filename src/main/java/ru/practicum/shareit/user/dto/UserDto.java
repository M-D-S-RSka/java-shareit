package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.markers.Markers;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    @NotNull(groups = Markers.Update.class)
    private Long id;

    private String name;

    @Email(groups = {Markers.Create.class, Markers.Update.class})
    @NotBlank(groups = Markers.Create.class)
    private String email;
}