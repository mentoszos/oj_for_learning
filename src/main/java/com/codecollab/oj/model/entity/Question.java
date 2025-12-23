package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 题目表
 * @TableName question
 */
@TableName(value ="question")
@Data
public class Question {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题目标签列表，需要转为字符串
     */
    @TableField(value = "tag")
    private String tag;

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
     * 时间限制，单位ms，默认1000
     */
    @TableField(value = "time_limit")
    private Integer timeLimit;

    /**
     * 内存限制，单位MB，默认256
     */
    @TableField(value = "memory_limit")
    private Integer memoryLimit;

    /**
     * 题目标题
     */
    @TableField(value = "title")
    private String title;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}