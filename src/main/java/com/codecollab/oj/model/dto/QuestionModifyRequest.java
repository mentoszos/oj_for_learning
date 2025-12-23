package com.codecollab.oj.model.dto;

import com.codecollab.oj.model.entity.QuestionUsecase;
import lombok.Data;
import org.apache.ibatis.javassist.SerialVersionUID;

import java.io.Serializable;
import java.util.List;

@Data
public class QuestionModifyRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private List<String> tags;
    private String content;
    private List<QuestionUsecaseModifyRequest> usecaseModifyRequestList;
}
