package com.codecollab.oj.model.dto;

import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.entity.CheckPoint;
import com.codecollab.oj.model.entity.ExecuteMessage;
import com.codecollab.oj.model.entity.JudgeInfo;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class ExecuteCodeResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

//    private JudgeInfo judgeInfo;
    private List<ExecuteMessage> executeMessages;
    private SubmitStatus submitStatus;
    private String errMsg; // 这个在执行一次代码的时候可以用来返回错误数据比如RE和CE



}
