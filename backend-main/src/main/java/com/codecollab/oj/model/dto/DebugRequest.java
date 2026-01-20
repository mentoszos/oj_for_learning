package com.codecollab.oj.model.dto;

import com.codecollab.oj.common.enums.SubmitLanguageType;
import lombok.Data;

import java.io.Serializable;

@Data
public class DebugRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer questionId;
    private String code;
    private SubmitLanguageType submitLanguageType;
    private String input;
}
