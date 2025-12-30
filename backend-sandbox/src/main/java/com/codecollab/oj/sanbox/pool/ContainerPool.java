package com.codecollab.oj.sanbox.pool;

import com.codecollab.oj.sanbox.docker.DockerManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ContainerPool {
    private final BlockingQueue<DockerContainer> containers = new LinkedBlockingQueue<>();
    @Resource
    private DockerManager dockerManager;


    @PostConstruct
    public void initPool(){
        this.initPool(1);
    }

    public void initPool(int num){
        for (int i =0;i<num; i++){
            DockerContainer container = dockerManager.createContainer("bellsoft/liberica-openjdk-alpine:8");
            containers.offer(container);
            dockerManager.startContainer(container.getContainerId());
        }

    }

    public DockerContainer borrowContainer(){
        return containers.poll();
    }

    public void returnContainer(DockerContainer container){
        containers.offer(container);
    }
}
