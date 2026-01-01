package com.codecollab.oj.sanbox.impl;

import cn.hutool.core.util.StrUtil;
import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.model.entity.CheckPoint;
import com.codecollab.oj.model.entity.JudgeInfo;
import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.constant.DockerExitCodeConstants;
import com.codecollab.oj.sanbox.model.ExecuteMessage;
import com.codecollab.oj.sanbox.pool.ContainerPool;
import com.codecollab.oj.sanbox.pool.DockerContainer;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
@Component
public class DockerCodeSandbox implements CodeSandbox {
    @Resource
    private ContainerPool containerPool;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws InterruptedException {
        DockerContainer container = containerPool.borrowContainer();
        try{
            //todo 从request里找到代码，把代码拷贝到容器
            String codeText = executeCodeRequest.getCode();
            container.copyCodeToContainer(codeText);

            //todo 启动javac，如果编译错误，返回CE
            ExecuteMessage emsg = container.compileCode();
            //todo 这里编译失败了也只会返回exitcode=1；
            if (emsg.getExitCode()!= DockerExitCodeConstants.SUCCESS) {
                return ExecuteCodeResponse.builder().submitStatus(SubmitStatus.CE)
                        .errMsg(emsg.getErrMessage())
                        .build();
            }

            //todo 从request里找到输入，注入代码中（通过输入流）多次运行，执行多次docker命令
            //todo 想办法获取多次的输出，是直接获取输出（推荐这个）还是输出到文件
            //判题
            List<String> inputs = executeCodeRequest.getInputs();
            List<Long> timeLimits = executeCodeRequest.getTimeLimits();
            List<String> answers = executeCodeRequest.getOutputs();
            List<Double> memoryLimits = executeCodeRequest.getMemoryLimits();

            List<String> outputs = new LinkedList<>();
            List<CheckPoint> checkPointList = new LinkedList<>();
            int total = 0, totalPass = 0;
            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);
                Long timeLimit = timeLimits.get(i);
                String ans = answers.get(i);
                double memoryLimit = memoryLimits.get(i);

                total += 1;
                ExecuteMessage executeMessage = container.executeCode(input, timeLimit,memoryLimit);
                CheckPoint checkPoint = CheckPoint.builder()
                        .accepted(SubmitStatus.ACCEPTED.getValue().intValue() == executeMessage.getExitCode().longValue())
                        .memory(executeMessage.getMemory())
                        .time(executeMessage.getTime())
                        .build();
                checkPointList.add(checkPoint);
                outputs.add(executeMessage.getOutput());

                //判断该checkpoint过没
                Long exitCode = executeMessage.getExitCode();
                if (executeMessage.getTimeout() || executeMessage.getTime().intValue() > timeLimit)
                    checkPoint.setSubmitStatus(SubmitStatus.TLE);
                else if (exitCode == DockerExitCodeConstants.MLE)
                    checkPoint.setSubmitStatus(SubmitStatus.MLE);
                else if (StrUtil.isNotEmpty(executeMessage.getErrMessage()))
                    checkPoint.setSubmitStatus(SubmitStatus.RE);
                else if (compareOutput(executeMessage.getOutput(), ans)) {
                    checkPoint.setSubmitStatus(SubmitStatus.ACCEPTED);
                    checkPoint.setAccepted(true);
                    totalPass += 1;
                } else checkPoint.setSubmitStatus(SubmitStatus.WA);


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
            ExecuteCodeResponse response = ExecuteCodeResponse.builder().outputs(outputs).judgeInfo(judgeInfo)
                    .submitStatus(finalStatus)
                    .build();


            return response;
        }

        finally {//todo 清理容器并归还容器
            //默认工作区为  /app
            container.deleteCode("");
            containerPool.returnContainer(container);
        }
    }

    private boolean compareOutput(String actual, String expected) {
        if (actual == null || expected == null) return false;
        // 1. 去掉首尾空白字符
        // 2. 将 \r\n 统一替换为 \n
        String a = actual.trim().replace("\r\n", "\n");
        String e = expected.trim().replace("\r\n", "\n");
        return a.equals(e);
    }
}
