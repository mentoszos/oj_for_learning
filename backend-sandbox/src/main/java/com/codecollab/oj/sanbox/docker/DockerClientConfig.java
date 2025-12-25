package com.codecollab.oj.sanbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DockerClientConfig {
    @Bean
    public DockerClient dockerClient(){
        DefaultDockerClientConfig dockerClientConfig = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerConfig("tcp://localhost:2375").build();

        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder().dockerHost(dockerClientConfig.getDockerHost()).build();
        return DockerClientImpl.getInstance(dockerClientConfig,httpClient);
    }
}
