package com.codecollab.oj.sanbox.impl;

import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.pool.ContainerPool;
import jakarta.annotation.Resource;

public class DockerCodeSandbox implements CodeSandbox {
    @Resource
    private ContainerPool containerPool;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String containerId = containerPool.borrowContainer();
        //todo 从request里找到代码，把代码拷贝到容器
        //todo 启动javac，如果编译错误，返回RE
        //todo 从request里找到输入，注入代码中（通过输入流）多次运行，执行多次docker命令
        //todo 想办法获取多次的输出，是直接获取输出（推荐这个）还是输出到文件
        //todo 将结果封装到response中
        //todo 清理容器并归还容器
    }
}
