package com.codecollab.oj.Compensator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.codecollab.oj.constants.MqConstants;
import com.codecollab.oj.mapper.MqMessageLogMapper;
import com.codecollab.oj.model.entity.MqMessageLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class MqMessageCompensator {

    @Resource
    private MqMessageLogMapper mqMessageLogMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;

    // 每 1 分钟运行一次
    @Scheduled(fixedRate = 60000)
    public void resendMessages() {
        // 1. 查询状态为 0 且更新时间超过 1 分钟的记录
        List<MqMessageLog> failMessages = mqMessageLogMapper.selectList(
                new QueryWrapper<MqMessageLog>()
                        .eq("status", 0)
                        .lt("update_time", LocalDateTime.now().minusMinutes(1))
        );

        for (MqMessageLog logRecord : failMessages) {
            try {
                // 2. 重新投递
                rabbitTemplate.convertAndSend(MqConstants.JUDGE_EXCHANGE_NAME, MqConstants.ROUTING_KEY, logRecord.getContent());

                // 3. 投递成功，修改状态
                logRecord.setStatus(1);
                mqMessageLogMapper.updateById(logRecord);
                log.info("消息补偿成功: {}", logRecord.getContent());
            } catch (Exception e) {
                // 4. 如果还是失败，增加重试次数，甚至可以设置最大重试次数转死信
                logRecord.setRetryCount(logRecord.getRetryCount() + 1);
                mqMessageLogMapper.updateById(logRecord);
            }
        }
    }
}