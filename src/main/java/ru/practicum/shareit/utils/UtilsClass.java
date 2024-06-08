package ru.practicum.shareit.utils;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@UtilityClass
public class UtilsClass {
    public static Pageable getPageable(Integer from, Integer size, Sort sort) {
        if (from != null && size != null) {
            return PageRequest.of(from / size, size, sort);
        } else {
            return Pageable.unpaged();
        }
    }

    public static Pageable getPageable(Integer from, Integer size) {
        if (from != null && size != null) {
            return PageRequest.of(from / size, size);
        } else {
            return Pageable.unpaged();
        }
    }
}