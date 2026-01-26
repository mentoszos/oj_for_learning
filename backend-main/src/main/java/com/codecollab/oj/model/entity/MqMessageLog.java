package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName mq_message_log
 */
@TableName(value ="mq_message_log")
@Data
public class MqMessageLog {
    /**
     * 消息ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 消息内容(如submitId)
     */
    @TableField(value = "content")
    private String content;

    /**
     * 状态: 0-投递中, 1-投递成功, 2-投递失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 重试次数
     */
    @TableField(value = "retry_count")
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    @TableField(value = "next_retry_time")
    private LocalDateTime nextRetryTime;

    /**
     * 
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
}