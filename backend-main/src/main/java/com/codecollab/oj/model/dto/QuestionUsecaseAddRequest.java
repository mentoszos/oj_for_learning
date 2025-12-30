package com.codecollab.oj.model.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionUsecaseAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 测试用例输入
     */
    private String input;

    /**
     * 测试用例输出
     */
    private String output;

    //问题的id
    private Integer questionId;

    //时间，ms
    private Integer timeLimit;
    //内存,MB
    private Double memoryLimit;
    //是否启用
    private Boolean active;
}
