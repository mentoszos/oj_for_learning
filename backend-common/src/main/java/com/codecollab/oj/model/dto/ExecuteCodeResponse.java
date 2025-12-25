package com.codecollab.oj.model.dto;

import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.entity.CheckPoint;
import com.codecollab.oj.model.entity.JudgeInfo;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExecuteCodeResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private JudgeInfo judgeInfo;
    private List<String> outputs;

    private SubmitStatus submitStatus;



}
