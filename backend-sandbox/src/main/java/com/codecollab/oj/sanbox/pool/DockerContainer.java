package com.codecollab.oj.sanbox.pool;

import cn.hutool.core.util.StrUtil;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import com.codecollab.oj.model.entity.ExecuteMessage;
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
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;

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
    public ExecuteMessage executeCode(long timeoutMilSeconds,double memoryLimit) throws InterruptedException {
        return this.executeCode(null, timeoutMilSeconds,memoryLimit);
    }

    public ExecuteMessage executeCode(String input, long timeoutMilSeconds, double memoryLimit) throws InterruptedException {
//        String[] cmd = {"java", "-cp", "/app", "Main"};
        if (memoryLimit<128) memoryLimit = 128;
        int memoryMB = (int) memoryLimit;
        int heapMB = (int) (memoryMB * 0.70);
        int metaspaceMB = (int) (memoryMB * 0.12);
        int directMB = (int) (memoryMB * 0.06);
        int stackTotalMB = 16;   // 固定预留
        int nativeMB = 32;       // JVM / libc / JIT buffer
        int sum = heapMB + metaspaceMB + directMB + stackTotalMB + nativeMB;
        if (sum > memoryMB) {
            heapMB -= (sum - memoryMB);
        }
        if (heapMB < 128) {
            throw new IllegalStateException("Heap too small after adjustment");
        }

        // ===== 2. 构造 Java 执行命令 =====
        String runCmd = String.format(
                "/usr/bin/time -v -o /app/report.txt " +
                        "java " +
                        "-Xms%dm " +
                        "-Xmx%dm " +
                        "-Xss512k " +
                        "-XX:MaxMetaspaceSize=%dm " +
                        "-XX:MaxDirectMemorySize=%dm " +
                        "-XX:+UseSerialGC " +
                        "-XX:+ExitOnOutOfMemoryError " +
                        "-cp /app Main",
                heapMB,
                heapMB,
                metaspaceMB,
                directMB
        );
        String[] cmd = {"sh", "-c", runCmd};

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
            @Override
            public void onError(Throwable throwable){
                //调用oncomplete,关掉Adapter
                super.onComplete();
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
//            long startCpuTime = getCpuTimeTOMilSeconds();
            long startWallTime = System.currentTimeMillis();
            long wallTimeLimit = timeoutMilSeconds*5;
            execStartCmd.exec(frameAdapter).awaitCompletion(wallTimeLimit,TimeUnit.MILLISECONDS);//用awaitcompletion能第一时间知道程序执行完,要么报错，要么等10秒，测试结果是程序执行完成后都会报错，如果执行不完就是5倍时间。
             //ms
            long endWallTime = System.currentTimeMillis();
            long WallTimeUsed = endWallTime-startWallTime;
            //获取endwalltime必须在获取endcputime之前，不然会导致walltimeused从97ms增到2000ms左右，
            long endCpuTime = getCpuTimeTOMilSeconds();
//            long cputimeUsed = endCpuTime - startCpuTime;


            // 2. 读取 /app/report.txt 的内容
            String reportContent = getContainerFileContent("/app/report.txt");
// 3. 解析出内存峰值
            double maxMemoryMBytes = parseMemoryToMB(reportContent);
            System.out.println("内存峰值 (MB): " + maxMemoryMBytes);
            long cputimeUsed = parseCpuTimeMs(reportContent);
            System.out.println("cpu耗时(ms): " + cputimeUsed);

            InspectExecResponse inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();
//
//            do{
//                Thread.sleep(1500);//1.5s
//                inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();
//            }
//            while(System.currentTimeMillis()-startWallTime<3000&&inspectExecResponse.isRunning());



            if (inspectExecResponse.isRunning()) {//因为要跑所有测试用例，所以这里不把代码删了
                this.stopProcesses();
            }


            //查看执行结果数据
            Long exitCodeLong = inspectExecResponse.getExitCodeLong();
            exitCodeLong = (exitCodeLong == null) ? -1L : Math.toIntExact(exitCodeLong);
            boolean timeout = WallTimeUsed>wallTimeLimit;//墙上时间大于5倍cpu要求时间说明有问题，给他tle；

            ExecuteMessage executeMessage = ExecuteMessage.builder()
                    .errMessage(err.toString().trim())
                    .output(out.toString().trim())
                    .exitCode(exitCodeLong)
                    .wallTimeout(timeout)
                    .time((int) cputimeUsed)
//                    .memory((maxMemory.get() / 1024.0 / 1024))
                    .memory(maxMemoryMBytes)
                    .build();
            return executeMessage;
        } catch (RuntimeException  e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            // 关闭输入流
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("关闭输入流失败", e);
            }
        }
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
    private void stopProcesses() {
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

    /**
     * 读取容器内的文件内容（将 report.txt 读成 String）
     */
    private String getContainerFileContent(String filePath) {
        // 1. 获取 Docker 的文件流（这是一个 Tar 包）
        try (InputStream dockerStream = dockerClient.copyArchiveFromContainerCmd(containerId, filePath).exec();
             TarArchiveInputStream tarStream = new TarArchiveInputStream(dockerStream)) {

            // 2. 移动到第一个文件条目 (我们的 report.txt)
            TarArchiveEntry entry = tarStream.getNextTarEntry();
            if (entry == null) {
                throw new RuntimeException("文件不存在: " + filePath);
            }

            // 3. 读取内容并转为字符串
            // 这里的 IOUtils 是 org.apache.commons.io 包下的，如果没有可以用 new String(tarStream.readAllBytes())
            return IOUtils.toString(tarStream, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("读取容器文件失败", e);
        }
    }
    // 修改返回值类型为 double
    private double parseMemoryToMB(String report) {
        String[] lines = report.split("\n");
        for (String line : lines) {
            if (line.contains("Maximum resident set size")) {
                String[] parts = line.split(":");
                if (parts.length > 1) {
                    String numStr = parts[1].trim();
                    // 原始单位是 kbytes
                    long kbytes = Long.parseLong(numStr);

                    // 除以 1024.0 得到 MB (保留小数位)
                    return kbytes / 1024.0;
                }
            }
        }
        return 0.0;
    }
    private long parseCpuTimeMs(String report) {
        String[] lines = report.split("\n");
        double userTime = 0.0;
        double sysTime = 0.0;

        for (String line : lines) {
            line = line.trim();
            // 1. 解析用户态时间
            if (line.contains("User time (seconds)")) {
                userTime = parseDoubleValue(line);
            }
            // 2. 解析内核态时间
            else if (line.contains("System time (seconds)")) {
                sysTime = parseDoubleValue(line);
            }
        }

        // 3. 计算总和并转换为毫秒 (1s = 1000ms)
        // 建议向上取整或者四舍五入，防止 0.001s 被截断成 0ms
        return (long) Math.ceil((userTime + sysTime) * 1000);
    }
    // 辅助方法：提取冒号后的浮点数
    private double parseDoubleValue(String line) {
        try {
            String[] parts = line.split(":");
            if (parts.length > 1) {
                return Double.parseDouble(parts[1].trim());
            }
        } catch (Exception e) {
            // 忽略解析错误，默认返回 0.0
        }
        return 0.0;
    }

}