package com.codecollab.oj.sanbox.impl;

import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.constant.DockerExitCodeConstants;
import com.codecollab.oj.model.entity.ExecuteMessage;
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
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest)  {
        DockerContainer container = containerPool.borrowContainer();
        try {
            //todo 从request里找到代码，把代码拷贝到容器
            String codeText = executeCodeRequest.getCode();
            container.copyCodeToContainer(codeText);

            //todo 启动javac，如果编译错误，返回CE
            ExecuteMessage emsg = this.compileCode(container);
            //todo 这里编译失败了也只会返回exitcode=1；
            if (emsg.getExitCode() != DockerExitCodeConstants.SUCCESS) {
                return ExecuteCodeResponse.builder().submitStatus(SubmitStatus.CE)
                        .errMsg(emsg.getErrMessage())
                        .build();
            }

            //判题
            List<String> inputs = executeCodeRequest.getInputs();
            List<Long> timeLimits = executeCodeRequest.getTimeLimits();
            List<Double> memoryLimits = executeCodeRequest.getMemoryLimits();

            String errmsg = null;
            List<ExecuteMessage> executeMessages = new LinkedList<>();
            ExecuteCodeResponse executeCodeResponse = ExecuteCodeResponse.builder()
                    .executeMessages(executeMessages)
                    .errMsg(errmsg)
                    .submitStatus(SubmitStatus.ACCEPTED)
                    .build();

            for (int i = 0; i < inputs.size(); i++) {
                String input = inputs.get(i);
                input = input+"\n"; //有些时候测试啥的输入没加回车，不加这回车，可能输入流就会一直等一直等然后就死了
                Long timeLimit = timeLimits.get(i);
                double memoryLimit = memoryLimits.get(i);
                ExecuteMessage executeMessage = container.executeCode(input, timeLimit, memoryLimit);
                executeMessages.add(executeMessage);
                if (executeMessage.getExitCode() != DockerExitCodeConstants.SUCCESS) {
                    executeCodeResponse.setSubmitStatus(SubmitStatus.ERROR);
                    executeCodeResponse.setErrMsg(executeMessage.getErrMessage());
                    break;
                }
            }
            return executeCodeResponse;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {//todo 清理容器并归还容器
            //默认工作区为  /app
            container.deleteCode("");
            containerPool.returnContainer(container);
        }
    }
    private ExecuteMessage compileCode(DockerContainer container){
        return container.compileCode();
    }

}
