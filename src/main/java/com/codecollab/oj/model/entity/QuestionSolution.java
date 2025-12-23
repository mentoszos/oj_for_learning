package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 题解
 * @TableName question_solution
 */
@TableName(value ="question_solution")
@Data
public class QuestionSolution implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题解内容
     */
    @TableField(value = "content")
    private Integer content;

    /**
     * 题目id
     */
    @TableField(value = "question_id")
    private Integer questionId;

    /**
     * 提交题解的用户id
     */
    @TableField(value = "user_id")
    private Integer userId;
}