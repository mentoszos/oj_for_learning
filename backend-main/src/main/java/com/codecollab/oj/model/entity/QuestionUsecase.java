package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * 题目测试用例
 * @TableName question_usecase
 */
@TableName(value ="question_usecase")
@Data
public class QuestionUsecase {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题目id
     */
    @TableField(value = "question_id")
    private Integer questionId;

    /**
     * 测试用例输入
     */
    @TableField(value = "input")
    private String input;

    /**
     * 测试用例输出
     */
    @TableField(value = "output")
    private String output;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     *
     */
    @TableField(value = "active")
    private Boolean active;


    @TableField(value = "number")
    private Integer number;
    /**
     * ms
     */
    @TableField(value = "time_limit")
    private Integer timeLimit;

    /**
     * MB
     */
    @TableField(value = "memory_limit")
    private Double memoryLimit;
}