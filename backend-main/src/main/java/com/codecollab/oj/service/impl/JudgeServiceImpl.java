package com.codecollab.oj.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.codecollab.oj.Manager.SseManager;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.constants.MqConstants;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.exception.BusinessException;
import com.codecollab.oj.mapper.MqMessageLogMapper;
import com.codecollab.oj.mapper.QuestionSubmitMapper;
import com.codecollab.oj.mapper.QuestionUsecaseMapper;
import com.codecollab.oj.model.dto.DebugRequest;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.*;
import com.codecollab.oj.model.vo.DebugVO;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.sanbox.constant.DockerExitCodeConstants;
import com.codecollab.oj.service.JudgeService;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
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
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
    private SseManager sseManager;
    @Autowired
    private MqMessageLogMapper mqMessageLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubmitResultVO submitCode(SubmitRequest request) {
        // 1. 创建提交记录
        QuestionSubmit submit = new QuestionSubmit();
        String code = request.getCode();
        Integer questionId = request.getQuestionId();
        SubmitLanguageType submitLanguageType = request.getSubmitLanguageType();

        submit.setSumbitCode(code);
        submit.setQuestionId(questionId);
        submit.setCodeLanguage(submitLanguageType);
        submit.setStatus(1);
        Integer userId = UserHolder.getUserId();

        submit.setUserId(userId);
// 2. 同时在同一个事务里，保存一条消息日志
        MqMessageLog messageLog = new MqMessageLog();
        messageLog.setId(IdWorker.getId()); // 生成一个分布式ID
        messageLog.setContent(String.valueOf(submit.getId()));
        messageLog.setStatus(0); // 投递中

        questionSubmitMapper.insert(submit);
        mqMessageLogMapper.insert(messageLog);

        //todo  2. 发送到消息队列（异步判题）
        try{
            rabbitTemplate.convertAndSend(MqConstants.JUDGE_EXCHANGE_NAME,MqConstants.ROUTING_KEY, submit.getId());
            messageLog.setStatus(1);
            mqMessageLogMapper.updateById(messageLog);
        }catch (Exception e){
            log.error("MQ 初次发送失败，等待定时任务补偿: {}", e.getMessage());
        }

        return SubmitResultVO.builder().id(submit.getId()).build();
    }

    @RabbitListener(queues = MqConstants.JUDGE_QUEUE)
    public SubmitResultVO onJudge(String submitId, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        QuestionSubmit submit = questionSubmitMapper.selectById(submitId);

        //做个幂等校验，如果已经判题了就别管他了，其实重新执行一次也是可以，但会浪费资源
        if (submit == null) {//查不到记录，应该不会出现这种情况
            channel.basicAck(deliveryTag, false);
            return null;
        }
        UpdateWrapper<QuestionSubmit> updateWrapper = new UpdateWrapper<QuestionSubmit>()
                .eq("id", submitId)
                .ne("status", 2) // 只有原状态是待判题，才允许更新
                .set("status", 2);
        int row = questionSubmitMapper.update(updateWrapper);
        if (row < 1){
            channel.basicAck(deliveryTag, false);
            return null;
        }

        Integer userId = submit.getUserId();
//        Integer userId = 1;
        String code = submit.getSumbitCode();
        SubmitLanguageType submitLanguageType = submit.getCodeLanguage();
        Integer questionId = submit.getQuestionId();

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

        //如果调用出现异常，不会设置为已完成判题,返回nack让他把消息重新放到队头
        try {
            CloseableHttpResponse res = http.execute(build);
            HttpEntity entity = res.getEntity();
            try {
                String string = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                log.info("接收到的判题消息内容: {}", string);
                executeCodeResponse = JSONUtil.toBean(string, ExecuteCodeResponse.class);

            } catch (ParseException e) {
                channel.basicNack(deliveryTag,false,true);
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            channel.basicNack(deliveryTag,false,true);
            throw new RuntimeException(e);
        }

        submit.setStatus(2);
        SubmitStatus submitStatus = executeCodeResponse.getSubmitStatus();
        if (submitStatus == SubmitStatus.CE) {
            submit.setSubmitStatus(submitStatus);
            submit.setErrMsg(executeCodeResponse.getErrMsg());
            questionSubmitMapper.updateById(submit);
            channel.basicAck(deliveryTag, false);
            sseManager.sendMessage(userId,SubmitResultVO.builder()
                            .submitStatus(submitStatus)
                            .errMsg(executeCodeResponse.getErrMsg())
                    .build());
            return null;
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
        submit.setJudgeInfo(judgeInfo);
        submit.setSubmitStatus(finalStatus);
        submit.setErrMsg(errMsg);
        questionSubmitMapper.updateById(submit);
        channel.basicAck(deliveryTag, false);
        sseManager.sendMessage(userId,SubmitResultVO.builder()
                .judgeInfo(judgeInfo)
                .submitStatus(submitStatus)
                .errMsg(executeCodeResponse.getErrMsg())
                .build());
        return null;
    }

    @Override
    @Async("judgeExecutor")
    public void submitCodeAsync(SubmitRequest request, SseEmitter emitter) {
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

//        questionSubmitMapper.insert(submit);

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
            submit.setSubmitStatus(submitStatus);
            submit.setStatus(3);
            questionSubmitMapper.insert(submit);
            SubmitResultVO submitResultVO = SubmitResultVO.builder()
                    .submitStatus(submitStatus)
                    .sumbitCode(code)
                    .errMsg(executeCodeResponse.getErrMsg())
                    .build();
            try {
                emitter.send(submitResultVO);
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"判题结果推送失败,IO出错");
            }
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
        submit.setJudgeInfo(judgeInfo);
        submit.setSubmitStatus(finalStatus);
        questionSubmitMapper.insert(submit);

        SubmitResultVO submitResultVO = SubmitResultVO.builder()
                .codeLanguage(submitLanguageType)
                .status(2)
                .sumbitCode(code)
                .judgeInfo(judgeInfo)
                .submitStatus(finalStatus)
                .errMsg(errMsg)
                .build();
        try {
            emitter.send(submitResultVO);
            emitter.complete();
        } catch (IOException e) {
            emitter.completeWithError(e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"判题结果推送失败,IO出错");
        }
    }

    @Override
    public DebugVO debugCode(DebugRequest request) {
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguageType(request.getSubmitLanguageType());
        executeCodeRequest.setCode(request.getCode());
        LambdaQueryWrapper<QuestionUsecase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select( QuestionUsecase::getTimeLimit, QuestionUsecase::getMemoryLimit);
        queryWrapper.eq(QuestionUsecase::getQuestionId, request.getQuestionId())
                .eq(QuestionUsecase::getActive, true)
                .last("limit 1");
        QuestionUsecase usecase = questionUsecaseMapper.selectOne(queryWrapper);
        ArrayList<String> inputs = new ArrayList<>();
        ArrayList<Long> timeLimits = new ArrayList<>();
        ArrayList<Double> memoryLimits = new ArrayList<>();
        inputs.add(request.getInput());
        timeLimits.add(Long.valueOf(usecase.getTimeLimit()));
        memoryLimits.add(usecase.getMemoryLimit());

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

        String errMsg = executeCodeResponse.getErrMsg();
        DebugVO debugVO = new DebugVO();
        debugVO.setErrMsg(errMsg);
        if (errMsg==null){
            ExecuteMessage executeMessage = executeCodeResponse.getExecuteMessages().get(0);
//        SubmitStatus submitStatus = executeCodeResponse.getSubmitStatus();
            debugVO.setErrMsg(executeMessage.getErrMessage());
            debugVO.setMemory(executeMessage.getMemory());
            debugVO.setTime(executeMessage.getTime());
//        debugVO.setSubmitStatus(submitStatus);
            debugVO.setOutputText(executeMessage.getOutput());
        }
        return debugVO;
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

