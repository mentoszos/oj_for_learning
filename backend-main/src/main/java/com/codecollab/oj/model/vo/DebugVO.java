package com.codecollab.oj.model.vo;

import com.codecollab.oj.common.enums.SubmitStatus;
import lombok.Data;

import java.io.Serializable;

@Data
public class DebugVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String outputText;
//    private SubmitStatus submitStatus;
    private String errMsg;
    private Double memory;
    private Integer time;
}
