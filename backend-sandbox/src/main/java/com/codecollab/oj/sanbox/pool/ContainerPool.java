package com.codecollab.oj.sanbox.pool;

import com.codecollab.oj.sanbox.docker.DockerManager;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ContainerPool {
    private final BlockingQueue<String> containerIds = new LinkedBlockingQueue<>();
    @Resource
    private DockerManager dockerManager;

    public void initPool(int num){
        for (int i =0;i<num; i++){
            String id = dockerManager.createContainer("bellsoft/liberica-openjdk-alpine:8");
            containerIds.offer(id);
        }
    }

    public String borrowContainer(){
        return containerIds.poll();
    }

    public void returnContainer(String id){
        containerIds.offer(id);
    }
}
