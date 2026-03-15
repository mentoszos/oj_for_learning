package com.codecollab.oj.sanbox.pool;

import com.codecollab.oj.sanbox.docker.DockerManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class ContainerPool {
    private final BlockingQueue<DockerContainer> containers = new LinkedBlockingQueue<>();
    private final ArrayList<String> allContainerIds = new ArrayList<>();
    @Resource
    private DockerManager dockerManager;

    /**
     * 池初始化大小（类似线程池 corePoolSize）
     */
    @Value("${sandbox.pool.init-size:1}")
    private int initSize;

    /**
     * 池中允许存在的最大容器数（类似线程池 maximumPoolSize）
     */
    @Value("${sandbox.pool.max-size:10}")
    private int maxSize;

    /**
     * 当前已经创建的容器总数，用于控制不超过 maxSize
     */
    private final AtomicInteger totalCreated = new AtomicInteger(0);

    /**
     * 容器空闲保活时间（毫秒），超过这个时间仍未被使用的多余容器会被销毁
     */
    @Value("${sandbox.pool.keep-alive-millis:300000}")
    private long keepAliveMillis;

    /**
     * 借阅容器超时时间（毫秒）。池满时最多等待这么久，超时则抛异常（拒绝策略，快速失败）。
     * 设为 0 或负数表示无限等待（沿用原 take() 阻塞行为）。
     */
    @Value("${sandbox.pool.borrow-timeout-ms:30000}")
    private long borrowTimeoutMs;

    /**
     * 记录每个容器上次“归还到池中”的时间
     */
    private final Map<String, Long> lastUsedTime = new ConcurrentHashMap<>();

    /**
     * 定时任务线程，用于定期清理空闲容器
     */
    private final ScheduledExecutorService evictor = Executors.newSingleThreadScheduledExecutor();


    @PostConstruct
    public void initPool(){
        this.initPool(initSize);
        // 启动定时清理任务
        startEvictor();
    }

    public void initPool(int num){
        for (int i =0;i<num; i++){
//            DockerContainer container = dockerManager.createContainer("bellsoft/liberica-openjdk-alpine:8");
            DockerContainer container = dockerManager.createContainer("oj-java:1.0");//内存监控需要这个
            containers.offer(container);
            allContainerIds.add(container.getFullContainerId());
            dockerManager.startContainer(container.getContainerId());
            totalCreated.incrementAndGet();
            // 初始创建的容器视为“刚刚使用过”
            lastUsedTime.put(container.getFullContainerId(), System.currentTimeMillis());
        }

    }

    public DockerContainer borrowContainer(){
        // 1. 先尝试从队列中直接获取一个可用容器（非阻塞）
        DockerContainer container = containers.poll();
        if (container != null) {
            return container;
        }

        // 2. 如果当前总数还没到上限，尝试创建新容器（控制在 maxSize 以内）
        if (totalCreated.get() < maxSize) {
            synchronized (this) {
                if (totalCreated.get() < maxSize) {
                    DockerContainer newContainer = dockerManager.createContainer("oj-java:1.0");//内存监控需要这个
                    allContainerIds.add(newContainer.getFullContainerId());
                    dockerManager.startContainer(newContainer.getContainerId());
                    totalCreated.incrementAndGet();
                    lastUsedTime.put(newContainer.getFullContainerId(), System.currentTimeMillis());
                    return newContainer;
                }
            }
        }

        // 3. 已达到最大容量：带超时的等待（拒绝策略），超时则快速失败，避免无限阻塞占满线程/连接
        try {
            if (borrowTimeoutMs > 0) {
                DockerContainer c = containers.poll(borrowTimeoutMs, TimeUnit.MILLISECONDS);
                if (c != null) return c;
                throw new IllegalStateException("获取容器超时，沙箱繁忙，请稍后重试（borrow-timeout-ms=" + borrowTimeoutMs + "）");
            }
            return containers.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取容器被中断", e);
        }
    }

    public void returnContainer(DockerContainer container){
        containers.offer(container);
        // 记录归还时间，作为“空闲开始计时点”
        lastUsedTime.put(container.getFullContainerId(), System.currentTimeMillis());
    }

    @PreDestroy
    public void stopAllContainers(){
        // 先停止定时任务
        evictor.shutdownNow();
        log.info("程序关闭，开始删除所有docker容器");
        for (String containerId: allContainerIds){
            try {
                dockerManager.stopContainer(containerId);
            } catch (Exception e) {
                log.warn("停止容器失败（可能已被提前删除）: {}", containerId, e);
            }
            try {
                dockerManager.removeContainer(containerId);
            } catch (Exception e) {
                log.warn("删除容器失败（可能已被提前删除）: {}", containerId, e);
            }
        }
    }

    /**
     * 启动定时清理任务，定期回收空闲容器
     */
    private void startEvictor() {
        // 间隔时间可以适当小于 keepAliveMillis，避免长时间堆积
        long interval = Math.max(keepAliveMillis / 2, 60_000L);
        evictor.scheduleAtFixedRate(this::evictIdleContainers, interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * 清理空闲时间超过 keepAliveMillis 的多余容器（只回收超过 initSize 之外的部分）
     */
    private void evictIdleContainers() {
        try {
            long now = System.currentTimeMillis();
            for (DockerContainer container : containers) {
                // 不要回收到达“核心数量”之前的容器
                if (totalCreated.get() <= initSize) {
                    return;
                }
                String id = container.getFullContainerId();
                Long last = lastUsedTime.get(id);
                if (last == null) {
                    continue;
                }
                if (now - last < keepAliveMillis) {
                    continue;
                }
                // 超时且当前总数大于核心数，尝试从队列中移除并销毁
                boolean removed = containers.remove(container);
                if (removed) {
                    log.info("回收空闲容器: {}", id);
                    lastUsedTime.remove(id);
                    totalCreated.decrementAndGet();
                    try {
                        dockerManager.stopContainer(id);
                    } catch (Exception e) {
                        log.warn("停止空闲容器失败: {}", id, e);
                    }
                    try {
                        dockerManager.removeContainer(id);
                    } catch (Exception e) {
                        log.warn("删除空闲容器失败: {}", id, e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("回收空闲容器时发生异常", e);
        }
    }
}
