package com.codecollab.oj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 面试房间表
 */
@Data
@TableName("interview_room")
public class InterviewRoom implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("room_uuid")
    private String roomUuid;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("question_id")
    private Long questionId;

    private Integer status; // 0-进行中, 1-已结束

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

