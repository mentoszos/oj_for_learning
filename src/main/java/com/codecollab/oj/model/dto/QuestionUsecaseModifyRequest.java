package com.codecollab.oj.model.dto;

import lombok.Data;

@Data
public class QuestionUsecaseModifyRequest {
    private Integer id;
    private String input;
    private String output;
}
