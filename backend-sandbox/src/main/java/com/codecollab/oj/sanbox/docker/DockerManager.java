package com.codecollab.oj.sanbox.docker;

import cn.hutool.core.lang.UUID;
import com.codecollab.oj.sanbox.pool.DockerContainer;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DockerManager {
    @Resource
    private DockerClient dockerClient;

    public DockerContainer createContainer(String image){
        String containerId = dockerClient.createContainerCmd(image).withHostConfig(new HostConfig().withMemory(256 * 1024 * 1024L)) //64MB
                .withName("oj-"+ UUID.randomUUID())
                .withNetworkDisabled(true)
                .withWorkingDir("/app")
                .withCmd("tail", "-f", "/dev/null") //给他一个命令，不然他启动了直接停止
                .exec().getId();
        return DockerContainer.builder()
                .containerId(containerId)
                .dockerClient(dockerClient)
                .build();
    }

    public void stopContainer(String containerId){
        dockerClient.stopContainerCmd(containerId).exec();
    }
    public void startContainer(String containerId){
        dockerClient.startContainerCmd(containerId).exec();
    }
    public void removeContainer(String containerId){
        dockerClient.removeContainerCmd(containerId).exec();
    }




}
