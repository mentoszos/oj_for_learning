package com.codecollab.oj.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionUsecaseModifyRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String input;
    private String output;
    private Boolean active;
    //时间，ms
    private Integer timeLimit;
    //内存,MB
    private Integer memoryLimit;
}
