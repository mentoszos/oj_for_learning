package com.codecollab.oj.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class QuestionUsecaseAddRequest {
    /**
     * 测试用例输入
     */
    private String input;

    /**
     * 测试用例输出
     */
    private String output;
}
