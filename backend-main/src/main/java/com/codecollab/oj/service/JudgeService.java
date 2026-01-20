package com.codecollab.oj.service;

import com.codecollab.oj.model.dto.DebugRequest;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.vo.DebugVO;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface JudgeService {


    /**
     * 获取判题结果
     */

    SubmitResultVO submitCode(SubmitRequest request);
    void submitCodeAsync(SubmitRequest request, SseEmitter emitter);



    DebugVO debugCode(DebugRequest request);
    SubmitResultVO onJudge(String submitId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException;
}

