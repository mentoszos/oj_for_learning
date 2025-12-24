package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 题目提交记录
 * @TableName question_submit
 */
@TableName(value ="question_submit")
@Data
public class QuestionSubmit {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 题目id
     */
    @TableField(value = "question_id")
    private Integer questionId;

    /**
     * 提交用户id
     */
    @TableField(value = "user_id")
    private Integer userId;

    /**
     * 判题状态 0-未提交 1-判题中 2-已通过 3-判题失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 判题结果详情，json存储{usercase：1 time：1200 memory：256 "message": "Runtime Error", "pass_count": 5, "total_count": 10}}
     */
    @TableField(value = "judge_info")
    private Object judgeInfo;

    /**
     * 本条记录创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 本条记录更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 提交的代码
     */
    @TableField(value = "sumbit_code")
    private String sumbitCode;

    /**
     * 代码类型，c、java
     */
    @TableField(value = "code_language")
    private String codeLanguage;
}