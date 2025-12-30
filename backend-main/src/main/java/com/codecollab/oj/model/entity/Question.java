package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

/**
 * 题目表
 * @TableName question
 */
@TableName(value ="question",autoResultMap = true)
@Data
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "usecase_count")
    private Integer usecaseCount;
    /**
     * 题目标签列表，需要转为字符串
     */
    @TableField(value = "tags",typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 该题的提交数
     */
    @TableField(value = "submit_num")
    private Integer submitNum;

    /**
     * 通过数
     */
    @TableField(value = "accepted_num")
    private Integer acceptedNum;

    /**
     * 创建者id
     */
    @TableField(value = "user_id")
    private Integer userId;



    /**
     * 题目标题
     */
    @TableField(value = "title")
    private String title;

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

}