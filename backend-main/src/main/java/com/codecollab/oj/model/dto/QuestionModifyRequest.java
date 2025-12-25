package com.codecollab.oj.model.dto;


import lombok.Data;


import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class QuestionModifyRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String title;
    private List<String> tags;
    private String content;
    private List<QuestionUsecaseModifyRequest> usecaseModifyRequestList;
}
