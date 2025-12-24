package com.codecollab.oj.service.impl;

import com.alibaba.fastjson2.JSON;
import com.codecollab.oj.mapper.QuestionMapper;
import com.codecollab.oj.mapper.QuestionSubmitMapper;
import com.codecollab.oj.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 判题服务实现（Demo版本，使用Mock判题）
 */
@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Autowired
    private QuestionSubmitMapper questionSubmitMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String JUDGE_QUEUE = "code_judge_queue";

    @Override
    public QuestionSubmit submitCode(Long questionId, String code, String language, Long userId) {
        // 1. 创建提交记录
        QuestionSubmit submit = new QuestionSubmit();
        submit.setQuestionId(questionId);
        submit.setCode(code);
        submit.setLanguage(language);
        submit.setUserId(userId);
        submit.setStatus(0); // 待判题

        questionSubmitMapper.insert(submit);

        // 2. 发送到消息队列（异步判题）
        rabbitTemplate.convertAndSend(JUDGE_QUEUE, submit.getId());

        // 3. 立即执行判题（Demo版本，实际应该由消费者处理）
        judgeCode(submit.getId());

        return submit;
    }

    @Override
    public QuestionSubmit getSubmitResult(Long submitId) {
        return questionSubmitMapper.selectById(submitId);
    }

    /**
     * 判题逻辑（Mock版本）
     * 实际应该使用Docker沙箱执行代码
     */
    private void judgeCode(Long submitId) {
        QuestionSubmit submit = questionSubmitMapper.selectById(submitId);
        if (submit == null) {
            return;
        }

        // 更新状态为判题中
        submit.setStatus(1);
        questionSubmitMapper.updateById(submit);

        try {
            // 模拟判题过程
            Thread.sleep(1000);

            // 获取题目信息
            Question question = questionMapper.selectById(submit.getQuestionId());
            if (question == null) {
                return;
            }

            // Mock判题结果（实际应该执行代码并对比输出）
            Map<String, Object> judgeInfo = new HashMap<>();
            judgeInfo.put("time", 100);
            judgeInfo.put("memory", 5000);

            // 简单判断：如果代码包含"main"方法，认为通过
            if (submit.getCode().contains("main") && submit.getCode().contains("Solution")) {
                submit.setStatus(2); // 成功
                judgeInfo.put("message", "Accepted");
            } else {
                submit.setStatus(3); // 失败
                judgeInfo.put("message", "Wrong Answer");
            }

            submit.setJudgeInfo(JSON.toJSONString(judgeInfo));
            questionSubmitMapper.updateById(submit);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("判题过程被中断", e);
        }
    }
}

