package ru.skillbox.social_network_post.web.util;

import org.springframework.data.domain.Sort;
import java.util.ArrayList;
import java.util.List;

public class SortParser {
    public static Sort parseSort(String[] sortParams) {
        List<Sort.Order> orders = new ArrayList<>();

        if (sortParams != null && sortParams.length % 2 == 0) {
            for (int i = 0; i < sortParams.length; i += 2) {
                String field = sortParams[i];
                Sort.Direction direction = Sort.Direction.fromString(sortParams[i + 1]);
                orders.add(new Sort.Order(direction, field));
            }
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}