package com.codecollab.oj.sanbox.pool;

import cn.hutool.core.util.StrUtil;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import com.codecollab.oj.sanbox.model.ExecuteMessage;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.InvocationBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Data
public class DockerContainer {
    private final String containerId;
    private final DockerClient dockerClient;
    private final String fullContainerId;

    @Builder
    public DockerContainer(String containerId,DockerClient dockerClient){
        this.dockerClient = dockerClient;
        this.containerId = containerId;
        this.fullContainerId = dockerClient.inspectContainerCmd(containerId).exec().getId();
    }
    public ExecuteMessage executeCode(long timeoutMilSeconds) throws InterruptedException {
        return this.executeCode(null, timeoutMilSeconds);
    }

    public ExecuteMessage executeCode(String input, long timeoutMilSeconds) throws InterruptedException {
        String[] cmd = {"java", "-cp", "/app", "Main"};
        boolean hasInput = false;
        if (StrUtil.isNotEmpty(input)) hasInput = true;
        ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId).withCmd(cmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(hasInput)
                .withTty(false)
                .exec();

        String execId = execResponse.getId();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDOUT.equals(streamType)) {
                    out.write(frame.getPayload(), 0, frame.getPayload().length);
                } else if (StreamType.STDERR.equals(streamType)) {
                    err.write(frame.getPayload(), 0, frame.getPayload().length);
                }
                super.onNext(frame);
            }
        };



        AtomicLong maxMemory = new AtomicLong(0L);

// 1. 订阅 Docker 统计信息流 内存监控
        ResultCallback<Statistics> statisticsCallback = dockerClient.statsCmd(fullContainerId)
                .exec(new ResultCallback.Adapter<Statistics>() {
                    @Override
                    public void onNext(Statistics stats) {
                        // 获取当前内存使用量（单位：字节）
                        Long usage = stats.getMemoryStats().getUsage();
                        if (usage != null) {
                            // 自动更新峰值
                            maxMemory.accumulateAndGet(usage, Math::max);
                        }
                    }
                });

        //运行cmd并记录时间
        InputStream inputStream = null;
        try {
            if (hasInput) {
                inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
            }
            ExecStartCmd execStartCmd = dockerClient.execStartCmd(execId);
            if(hasInput){
                execStartCmd.withStdIn(inputStream);
            }
            long startTime = getCpuTimeTOMilSeconds();
            ResultCallback.Adapter<Frame> exec = execStartCmd.exec(frameAdapter);
            if (inputStream!=null) inputStream.close();
            //给网络阻塞等原因留点时间，传进来的timmeoutseconds还是cpu执行的时间，这里只是预留一下给他加0.5s
            boolean completion = exec.awaitCompletion(timeoutMilSeconds + 500, TimeUnit.MILLISECONDS);
            long endTime = getCpuTimeTOMilSeconds();
            long cputimeUsed = endTime - startTime; //ms
            if (Boolean.FALSE.equals(completion)) {//因为要跑所有测试用例，所以这里不把代码删了
                this.stopProcesses();
            }


            //查看执行结果数据
            InspectExecResponse inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();
            Long exitCodeLong = inspectExecResponse.getExitCodeLong();
            Integer exitCode = (exitCodeLong == null) ? -1 : Math.toIntExact(exitCodeLong);
            ExecuteMessage executeMessage = ExecuteMessage.builder().errMessage(err.toString().trim())
                    .output(out.toString().trim())
                    .exitCode(exitCode == null ? -1L : exitCode)//超时、错误等导致他没有返回结果时，他会为null，但这个可以先判断completion来判断
                    .Timeout(!completion)
                    .time((int) cputimeUsed)
                    .memory((maxMemory.get() / 1024.0 / 1024))
                    .build();
            return executeMessage;
        } catch (RuntimeException  e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } catch (IOException e) {
            log.warn("关闭 测试用例inputstream 失败", e);
        } finally {
            // 关闭 frameAdapter - 这是导致管道错误的主要原因
            try {
                if (frameAdapter != null) {
                    frameAdapter.close();
                }
            } catch (IOException e) {
                log.warn("关闭 frameAdapter 失败", e);
            }
            // 关闭 statisticsCallback
            try {
                if (statisticsCallback != null) {
                    statisticsCallback.close();
                }
            } catch (IOException e) {
                log.warn("关闭内存监控流失败", e);
            }
            // 关闭输入流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
        return null;
    }


    private void restartContainer(){
        dockerClient.restartContainerCmd(containerId).withTimeout(0).exec();

    }

    private long getCpuTimeTOMilSeconds() {
        try {
            // 1. 发起 stats 命令，获取一次快照
            // .withNoStream(true) 表示只获取当前瞬间的数据，不需要持续监听
            Statistics stats = dockerClient.statsCmd(containerId)
                    .withNoStream(true)
                    .exec(new InvocationBuilder.AsyncResultCallback<Statistics>())
                    .awaitResult();
            if (stats != null && stats.getCpuStats() != null) {
                // 2. 获取 CPU 总消耗时间 (纳秒为单位)
                Long totalUsage = stats.getCpuStats().getCpuUsage().getTotalUsage();
                if (totalUsage != null) {
                    // 3. 纳秒转为毫秒
                    return totalUsage / 1_000_000;
                }
            }
        } catch (Exception e) {
            log.error("获取容器cpu时间信息失败", e);
        }
        return 0L;
    }

    /**
     * 停止容器内所有正在运行的 Java 进程
     */
    public void stopProcesses() {
        // pkill -9 -f java 会杀掉进程名中包含 java 的所有进程
        String[] cmd = {"sh", "-c", "pkill -9 -f java"};
        try {
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmd)
                    .exec();
            // 这种清理命令通常执行极快，给 1 秒超时绰绰有余
            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion(1, TimeUnit.SECONDS);
            log.info("容器 {} 进程清理完成", containerId);
        } catch (Exception e) {
            log.error("停止容器内进程失败: ", e);
            // 如果这里失败了，说明容器可能卡死了，这时候建议标记 isDirty = true，强制重启
        }
    }
    /**
     * 删除容器内指定的代码文件和字节码文件
     * @param workDir 代码存放的路径，例如 "/app"
     */
    public void deleteCode(String workDir) {
        // rm -rf 确保删除目录下的所有 Main.java, Main.class 以及可能的临时文件
        workDir = "/app";
        String[] cmd = {"sh", "-c", "rm -rf " + workDir + "/*"};
//        String[] cmd = {"sh", "-c", "pkill -9 -u mentos || true; rm -rf /app/* /tmp/*"};
        try {
            ExecCreateCmdResponse execResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmd)
                    .exec();
            dockerClient.execStartCmd(execResponse.getId())
                    .exec(new ResultCallback.Adapter<>())
                    .awaitCompletion(1, TimeUnit.SECONDS);
            log.info("容器 {} 代码文件已删除", containerId);
        } catch (Exception e) {
            log.error("删除容器内文件失败: ", e);
        }
    }

    public ExecuteMessage compileCode(){
        String[] compileCmd = {"javac", "-encoding", "utf-8", "Main.java"};
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(compileCmd).withAttachStdout(true).withAttachStderr(true).exec();
        String execId = execCreateCmdResponse.getId();
        StringBuilder errMsg = new StringBuilder();
        ResultCallback.Adapter<Frame> adapter = new ResultCallback.Adapter<>(){
            @Override
            public void onNext(Frame frame) {
                if (StreamType.STDERR == frame.getStreamType()) errMsg.append(new String(frame.getPayload()));
                super.onNext(frame);
            }
        };

        try {
            dockerClient.execStartCmd(execId)
                    .exec(adapter).awaitCompletion(5,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"编译代码失败");
        }

        InspectExecResponse inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();
        Long exitCode = inspectExecResponse.getExitCodeLong();
//        if (exitCode!=null && exitCode==0){
//            return true;
//        }
//        else return errMsg.toString();
        return ExecuteMessage.builder().exitCode(exitCode).errMessage(errMsg.toString()).build();
    }


    public void copyCodeToContainer(String code){
        byte[] bytes = code.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try(TarArchiveOutputStream tos = new TarArchiveOutputStream(bos)){
            TarArchiveEntry tarArchiveEntry = new TarArchiveEntry("Main.java");
            tarArchiveEntry.setSize(bytes.length);
            tarArchiveEntry.setMode(0644);
            tos.putArchiveEntry(tarArchiveEntry);
            tos.write(bytes);
            tos.closeArchiveEntry();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"内存打包tar失败");
        }


        try (InputStream inputStream = new ByteArrayInputStream(bos.toByteArray())){
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withTarInputStream(inputStream)
                    .withRemotePath("/app")
                    .exec();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"容器写入失败");
        }
    }
}