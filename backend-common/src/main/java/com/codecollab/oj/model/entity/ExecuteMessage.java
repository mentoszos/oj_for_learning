package com.codecollab.oj.model.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExecuteMessage {
    private Long exitCode;
    private String output;
    private String errMessage;

    private Integer time; // 单位ms
    private Double memory; // 单位MB
    private Boolean wallTimeout;
}
