package com.codecollab.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionUsecaseQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer questionId;
    private Integer currentPage;
    private Integer pageSize;
}
