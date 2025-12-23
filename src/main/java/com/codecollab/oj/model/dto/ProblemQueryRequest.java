package com.codecollab.oj.model.dto;

import lombok.Data;

@Data
public class ProblemQueryRequest {
    private Long id;
    private String title;
    //json
    private String tags;
    private Integer current = 1;
    private Integer pageSize = 10;
}

