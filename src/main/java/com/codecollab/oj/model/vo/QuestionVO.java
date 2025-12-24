package com.codecollab.oj.model.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.codecollab.oj.model.entity.QuestionInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目视图对象
 */
@Data
public class QuestionVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private List<String> tags;
    private Integer submitNum;
    private Integer acceptedNum;


    private String title;
    private Integer usecaseCount;
    private String content;
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    private QuestionInfo questionInfo;
}

