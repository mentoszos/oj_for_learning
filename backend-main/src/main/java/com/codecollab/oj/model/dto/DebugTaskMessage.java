package com.codecollab.oj.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Debug 任务 MQ 消息体：用于 debug 队列的发送与消费，携带请求 ID 和调试请求参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DebugTaskMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 本次 debug 请求唯一 ID，消费者完成后将结果写入 Redis debug:result:{requestId} */
    private String requestId;
    private DebugRequest request;
}
