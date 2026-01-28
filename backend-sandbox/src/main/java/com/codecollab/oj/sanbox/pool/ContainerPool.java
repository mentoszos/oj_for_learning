package com.codecollab.oj.sanbox.pool;

import com.codecollab.oj.sanbox.docker.DockerManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
public class ContainerPool {
    private final BlockingQueue<DockerContainer> containers = new LinkedBlockingQueue<>();
    private final ArrayList<String> allContainerIds = new ArrayList<>();
    @Resource
    private DockerManager dockerManager;


    @PostConstruct
    public void initPool(){
        this.initPool(1);
    }

    public void initPool(int num){
        for (int i =0;i<num; i++){
//            DockerContainer container = dockerManager.createContainer("bellsoft/liberica-openjdk-alpine:8");
            DockerContainer container = dockerManager.createContainer("oj-java:1.0");//内存监控需要这个
            containers.offer(container);
            allContainerIds.add(container.getFullContainerId());
            dockerManager.startContainer(container.getContainerId());
        }

    }

    public DockerContainer borrowContainer(){
        DockerContainer poll = containers.poll();
        if (poll == null) {
            DockerContainer container = dockerManager.createContainer("oj-java:1.0");//内存监控需要这个
            containers.offer(container);
            allContainerIds.add(container.getFullContainerId());
            dockerManager.startContainer(container.getContainerId());
            poll = container;
        }
        return poll;
    }

    public void returnContainer(DockerContainer container){
        containers.offer(container);
    }

    @PreDestroy
    public void stopAllContainers(){
        log.info("程序关闭，开始删除所有docker容器");
        for (String containerId: allContainerIds){
            dockerManager.stopContainer(containerId);
            dockerManager.removeContainer(containerId);
        }
    }
}
