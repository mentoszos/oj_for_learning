package com.codecollab.oj.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionQueryRequest implements Serializable {
    private static final long serialVersionUID = 1L;
//    private Long id;
    private String title;
    //json
    private List<String> tags;
    private Integer current = 1;
    private Integer pageSize = 10;
}

