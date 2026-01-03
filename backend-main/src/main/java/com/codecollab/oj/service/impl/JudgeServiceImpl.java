package com.codecollab.oj.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
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
import com.codecollab.oj.model.entity.*;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.constant.DockerExitCodeConstants;
import com.codecollab.oj.service.JudgeService;
import com.codecollab.oj.service.QuestionUsecaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.apache.http.client.HttpClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    @Autowired
    private CodeSandbox codeSandbox;

    private static final String JUDGE_QUEUE = "code_judge_queue";

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
        submit.setUserId(1);

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
        ArrayList<String> answers = new ArrayList<>();
        ArrayList<Long> timeLimits = new ArrayList<>();
        ArrayList<Double> memoryLimits = new ArrayList<>();
        for (QuestionUsecase usecase : usecaseList) {
            inputs.add(usecase.getInput());
            answers.add(usecase.getOutput());
            timeLimits.add(Long.valueOf(usecase.getTimeLimit()));
            memoryLimits.add(usecase.getMemoryLimit());
        }
        executeCodeRequest.setInputs(inputs);
        executeCodeRequest.setMemoryLimits(memoryLimits);
        executeCodeRequest.setTimeLimits(timeLimits);

//        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        String jsonString = JSONObject.toJSONString(executeCodeRequest);
        CloseableHttpClient http = HttpClientBuilder.create().build();
        ClassicHttpRequest build = ClassicRequestBuilder.post("http://localhost:8801/sandbox/execute").setEntity(jsonString, ContentType.APPLICATION_JSON).build();
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            CloseableHttpResponse res = http.execute(build);
            HttpEntity entity = res.getEntity();
            try {
                String string = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                executeCodeResponse = JSONUtil.toBean(string, ExecuteCodeResponse.class);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        SubmitStatus submitStatus = executeCodeResponse.getSubmitStatus();
        if (submitStatus == SubmitStatus.CE) {
            return SubmitResultVO.builder()
                    .submitStatus(submitStatus)
                    .sumbitCode(code)
                    .errMsg(executeCodeResponse.getErrMsg())
                    .build();
        }
        List<CheckPoint> checkPointList = new LinkedList<>();
        int index = 0;
        int total = 0, totalPass = 0;
        for (ExecuteMessage executeMessage : executeCodeResponse.getExecuteMessages()) {
            total += 1;
            String errMessage = executeMessage.getErrMessage();
            Long exitCode = executeMessage.getExitCode();
            Double memory = executeMessage.getMemory();
            String output = executeMessage.getOutput();
            Integer time = executeMessage.getTime();
            Boolean wallTimeout = executeMessage.getWallTimeout();
            CheckPoint checkPoint = new CheckPoint();
            checkPointList.add(checkPoint);

            if (Boolean.TRUE.equals(wallTimeout) || time > timeLimits.get(index))
                checkPoint.setSubmitStatus(SubmitStatus.TLE);
            else if (memory > memoryLimits.get(index) || exitCode == DockerExitCodeConstants.MLE)
                checkPoint.setMemory(memory);
            else if (StrUtil.isNotEmpty(errMessage) || exitCode == DockerExitCodeConstants.CE_OR_RE)
                checkPoint.setSubmitStatus(SubmitStatus.RE);
            else if (compareOutput(output, answers.get(index))) {
                checkPoint.setSubmitStatus(SubmitStatus.ACCEPTED);
                totalPass++;
            } else checkPoint.setSubmitStatus(SubmitStatus.WA);

            checkPoint.setMemory(memory);
            checkPoint.setAccepted(checkPoint.getSubmitStatus() == SubmitStatus.ACCEPTED);
            checkPoint.setTime(time);
            index++;
        }

        // 1. 寻找第一个非 ACCEPTED 的状态作为全局状态
        SubmitStatus finalStatus = SubmitStatus.ACCEPTED;
        for (CheckPoint cp : checkPointList) {
            if (cp.getSubmitStatus() != SubmitStatus.ACCEPTED) {
                finalStatus = cp.getSubmitStatus(); // 捕获第一个错误（如 TLE, MLE, RE）
                break;
            }
        }
        //todo 将结果封装到response中
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setCheckPoints(checkPointList);
        judgeInfo.setTotal(total);
        judgeInfo.setTotalPass(totalPass);
        String errMsg = executeCodeResponse.getErrMsg();

        return SubmitResultVO.builder()
                .codeLanguage(submitLanguageType.getValue())
                .status(2)
                .sumbitCode(code)
                .judgeInfo(judgeInfo)
                .submitStatus(finalStatus)
                .errMsg(errMsg)
                .build();
    }


    //校验输出与答案是否正确
    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) return false;
        // 1. 去掉首尾空白字符
        // 2. 将 \r\n 统一替换为 \n
        String a = actual.trim().replace("\r\n", "\n");
        String e = expected.trim().replace("\r\n", "\n");
        return a.equals(e);
    }
}

