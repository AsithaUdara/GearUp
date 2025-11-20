package com.gearup.templateservice.dto;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PageResult<T> {
    private List<T> items;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
