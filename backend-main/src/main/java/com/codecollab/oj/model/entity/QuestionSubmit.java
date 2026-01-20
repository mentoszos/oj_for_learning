package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目提交记录
 * @TableName question_submit
 */
@TableName(value ="question_submit",autoResultMap = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionSubmit implements Serializable {
    private static final long serialVersionUID = 1L;
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
    @TableField(value = "judge_info",typeHandler = JacksonTypeHandler.class)
    private JudgeInfo judgeInfo;

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
     * 提交的代码
     */
    @TableField(value = "sumbit_code")
    private String sumbitCode;

    /**
     * 代码类型，c、java
     */
    @TableField(value = "code_language")
    private SubmitLanguageType codeLanguage;

    @TableField(value = "submit_status")
    private SubmitStatus submitStatus;

    @TableField(value = "error_message")
    private String errMsg;
}