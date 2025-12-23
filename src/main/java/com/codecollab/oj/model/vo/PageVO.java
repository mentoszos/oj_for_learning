package com.codecollab.oj.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> records;
    private Long currentPage;
    private Long pageSize;
    private Long total;
}
