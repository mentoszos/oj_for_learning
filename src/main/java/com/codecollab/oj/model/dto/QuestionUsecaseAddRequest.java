package com.codecollab.oj.model.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionUsecaseAddRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 测试用例输入
     */
    private String input;

    /**
     * 测试用例输出
     */
    private String output;
}
