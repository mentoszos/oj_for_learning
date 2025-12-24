package com.codecollab.oj.model.dto;

import com.codecollab.oj.model.entity.QuestionUsecase;
import lombok.Data;

import java.util.List;

@Data
public class QuestionAddRequest {
    /**
     * 题目标签列表，列表
     */
    private List<String> tag;

    /**
     * 时间限制，单位ms，默认1000
     */
    private Integer timeLimit;

    /**
     * 内存限制，单位MB，默认256
     */
    private Integer memoryLimit;

    /**
     * 题目标题
     */
    private String title;

    /**
    * 题目内容
    */
    private String content;

    //测试用例
    private List<QuestionUsecaseAddRequest> usecases;
}
