package com.codecollab.oj.sanbox.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecuteMessage {
    private Long exitCode;
    private String output;
    private String errMessage;

    private Integer time; // 单位ms
    private Integer memory; // 单位MB
    private Boolean Timeout;// 是否触发了 Java 侧的超时中断
}
