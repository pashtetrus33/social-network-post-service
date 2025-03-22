package ru.skillbox.social_network_post.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageAccountDto {
    private int totalPages;
    private long totalElements;
    private PageableObject pageable;
    private int size;
    private List<AccountDto> content;
    private int number;
    private SortObject sort;
    private boolean first;
    private boolean last;
    private int numberOfElements;
    private boolean empty;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static
    class SortObject {
        private boolean sorted;
        private boolean empty;
        private boolean unsorted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static
    class PageableObject {
        private boolean paged;
        private int pageNumber;
        private int pageSize;
        private int offset;
        private SortObject sortObject;
        private boolean unpaged;
    }
}