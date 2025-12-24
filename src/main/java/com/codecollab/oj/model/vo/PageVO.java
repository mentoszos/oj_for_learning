package com.codecollab.oj.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> records;
    private Long currentPage;
    private Long pageSize;
    private Long total;
    private Long totalPages;
}
