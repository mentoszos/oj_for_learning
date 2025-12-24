package com.codecollab.oj.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UsecaseVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer questionId;
    private Integer sortNum; // 给前端展示的时候用于排序的字段
    private String input;
    private String output;
    private Boolean active;
    private Integer timeLimit;
    private Integer memoryLimit;

}
