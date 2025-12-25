package com.codecollab.oj.sanbox.docker;

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

    public String createContainer(String image){
        return dockerClient.createContainerCmd(image).withHostConfig(new HostConfig().withMemory(64*1024*1024L)) //64MB
                .withNetworkDisabled(true)
                .exec().getId();
    }

    public void stopContainer(String containerId){
        dockerClient.stopContainerCmd(containerId).exec();
    }


}
