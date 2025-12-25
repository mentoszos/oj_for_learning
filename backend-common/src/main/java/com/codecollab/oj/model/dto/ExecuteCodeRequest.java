package com.codecollab.oj.model.dto;

import com.codecollab.oj.common.enums.SubmitLanguageType;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExecuteCodeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID=1L;

    private String code;
    private SubmitLanguageType languageType;
    private List<String> inputs;

}
