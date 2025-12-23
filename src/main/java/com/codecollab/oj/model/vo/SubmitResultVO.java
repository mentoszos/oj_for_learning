package com.codecollab.oj.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交结果视图对象
 */
@Data
public class SubmitResultVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer status; // 0-待判题, 1-判题中, 2-成功, 3-失败
    private JudgeInfo judgeInfo;

    @Data
    public static class JudgeInfo {
        private Integer time;
        private Integer memory;
        private String message;
    }
}

