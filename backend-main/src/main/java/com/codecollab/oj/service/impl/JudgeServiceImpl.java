package com.codecollab.oj.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.mapper.QuestionMapper;
import com.codecollab.oj.mapper.QuestionSubmitMapper;
import com.codecollab.oj.mapper.QuestionUsecaseMapper;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.JudgeInfo;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.Factory.CodeSandboxFactory;
import com.codecollab.oj.service.JudgeService;
import com.codecollab.oj.service.QuestionUsecaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 判题服务实现（Demo版本，使用Mock判题）
 */
@Slf4j
@Service
public class JudgeServiceImpl implements JudgeService {

    @Autowired
    private QuestionSubmitMapper questionSubmitMapper;

    @Autowired
    private QuestionUsecaseMapper questionUsecaseMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private CodeSandbox codeSandbox;

    private static final String JUDGE_QUEUE = "code_judge_queue";

    public JudgeServiceImpl() {
        codeSandbox = CodeSandboxFactory.newInstance(CodeSandboxFactory.DOCKER_TYPE);
    }

    @Override
    public QuestionSubmit submitCode(Long questionId, String code, String language, Long userId) {
        return null;
    }

    @Override
    public QuestionSubmit getSubmitResult(Long submitId) {
        return questionSubmitMapper.selectById(submitId);
    }

    @Override
    public SubmitResultVO submitCode(SubmitRequest request) {
        // 1. 创建提交记录
        QuestionSubmit submit = new QuestionSubmit();
        String code = request.getCode();
        Integer questionId = request.getQuestionId();
        SubmitLanguageType submitLanguageType = request.getSubmitLanguageType();

        submit.setSumbitCode(code);
        submit.setQuestionId(questionId);
        submit.setCodeLanguage(submitLanguageType);
        submit.setStatus(2);
        submit.setUserId(UserHolder.getUserId());

        questionSubmitMapper.insert(submit);

        //todo  2. 发送到消息队列（异步判题）
//        rabbitTemplate.convertAndSend(JUDGE_QUEUE, submit.getId());

        // 立即执行判题（Demo版本，实际应该由消费者处理）
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguageType(submitLanguageType);
        executeCodeRequest.setCode(code);

        LambdaQueryWrapper<QuestionUsecase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(QuestionUsecase::getInput, QuestionUsecase::getOutput, QuestionUsecase::getTimeLimit, QuestionUsecase::getMemoryLimit);
        queryWrapper.eq(QuestionUsecase::getQuestionId, questionId)
                .eq(QuestionUsecase::getActive, true);
        List<QuestionUsecase> usecaseList = questionUsecaseMapper.selectList(queryWrapper);
        ArrayList<String> inputs = new ArrayList<>();
        ArrayList<String> outputs = new ArrayList<>();
        ArrayList<Long> timeLimits = new ArrayList<>();
        ArrayList<Double> memoryLimits = new ArrayList<>();
        executeCodeRequest.setInputs(inputs);
        executeCodeRequest.setOutputs(outputs);
        executeCodeRequest.setMemoryLimits(memoryLimits);
        executeCodeRequest.setTimeLimits(timeLimits);

        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        SubmitStatus submitStatus = executeCodeResponse.getSubmitStatus();
        JudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
        String errMsg = executeCodeResponse.getErrMsg();

        return SubmitResultVO.builder()
                .codeLanguage(submitLanguageType.getValue())
                .status(2)
                .sumbitCode(code)
                .judgeInfo(judgeInfo)
                .submitStatus(submitStatus)
                .errMsg(errMsg)
                .build();
    }


}

