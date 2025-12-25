package com.codecollab.oj.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.codecollab.oj.model.entity.JudgeInfo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提交结果视图对象
 */
@Data
public class SubmitResultVO implements Serializable {
    private static final long serialVersionUID = 1L;

//    private Long id;
//    /**
//     * 题目id
//     */
//    private Integer questionId;
//
//    /**
//     * 提交用户id
//     */
//    private Integer userId;

    /**
     * 判题状态 0-未提交 1-判题中 2-已通过 3-判题失败
     */
    private Integer status;

    /**
     * 判题结果详情，json存储{usercase：1 time：1200 memory：256 "message": "Runtime Error", "pass_count": 5, "total_count": 10}}
     */
    private JudgeInfo judgeInfo;

    private String sumbitCode;

    private String codeLanguage;
}

