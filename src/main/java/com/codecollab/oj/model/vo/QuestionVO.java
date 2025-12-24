package com.codecollab.oj.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 题目视图对象
 */
@Data
public class QuestionVO {
    private Long id;
    private String title;
    private String content;
    private List<String> tags;
    private Integer submitNum;
    private Integer acceptedNum;
    private JudgeConfig judgeConfig;

    @Data
    public static class JudgeConfig {
        private Integer timeLimit;
        private Integer memoryLimit;
    }
}

