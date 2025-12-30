package com.codecollab.oj.sanbox.pool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.exception.BusinessException;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.model.ExecuteMessage;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectExecResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import io.swagger.v3.oas.models.media.Content;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.bouncycastle.asn1.cms.Time;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Data

public class DockerContainer {
    private final String containerId;
    private final DockerClient dockerClient;
    public final String fullContainerId;
    public final String type;
    public final String memPath;

    @Builder
    public DockerContainer(String containerId,DockerClient dockerClient){
        this.dockerClient = dockerClient;
        this.containerId = containerId;
        this.fullContainerId = dockerClient.inspectContainerCmd(containerId).exec().getId();
        String[] memPathResult = getMemPath();
        memPath = memPathResult[0];
        type = memPathResult[1];
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
//        final long[] maxMemory = {0L};
//        ResultCallback<Statistics> statsCallback = dockerClient.statsCmd(containerId).exec(new ResultCallback.Adapter<Statistics>(){
//            @Override
//            public void onNext(Statistics stats) {
//                long curMemory = 0;
//                if(ObjectUtil.isNotEmpty(stats.getMemoryStats().getUsage()))
//                    curMemory = stats.getMemoryStats().getUsage();//这里不能用maxUsage，他记录的是容器创建以来的最大值，只能监控
//                //方法里能自动传对象，但是很难把 long maxMemory这种局部变量传进去还能实时修改
//                maxMemory[0] = Math.max(maxMemory[0], curMemory);//居然只能是对象，idea能标出来，太智能了
//            }
//        });statsCallback.close();


        //如果你把 Java 写的沙箱程序也打包成 Docker 运行（即 Docker-in-Docker），你的 Java 程序是读不到宿主机 /sys/fs/cgroup 的。
        //解决方法： 在启动“沙箱宿主容器”时，必须把宿主机的 cgroup 目录挂载进去：
        final AtomicLong maxMemory = new AtomicLong(0);
        final AtomicBoolean isRunning = new AtomicBoolean(true);
        String finalMemPath = memPath;//创这个变量是为了满足匿名内部类中引用变量需要为final，前面mempath变了，不是final；

        //内存监控
        Thread monitorThread = new Thread(() -> {
            while (isRunning.get()) {
                try {
                    String content = Files.readString(Paths.get(finalMemPath)).trim();
                    if (StrUtil.isNotEmpty(content)) {
                        long currentUsage = Long.parseLong(content);
                        maxMemory.accumulateAndGet(currentUsage, Math::max);
                    }
                    Thread.sleep(10);//采样10ms
                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "内存采样睡觉报错了");
                }
            }
        });
        monitorThread.start();


        //运行cmd并记录时间

        try(InputStream inputStream = hasInput? new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)):null) {
            ExecStartCmd execStartCmd = dockerClient.execStartCmd(execId);
            if(hasInput){
                execStartCmd.withStdIn(inputStream);
            }
            long startTime = getCpuTimeTOSeconds();
            //给网络阻塞等原因留点时间，传进来的timmeoutseconds还是cpu执行的时间，这里只是预留一下给他加0.5s
            boolean completion = execStartCmd.exec(frameAdapter).awaitCompletion(timeoutMilSeconds + 500, TimeUnit.MILLISECONDS);
            long endTime = getCpuTimeTOSeconds();
            long cputimeUsed = endTime - startTime; //ms
            if (Boolean.FALSE.equals(completion)) {//因为要跑所有测试用例，所以这里不把代码删了
                this.stopProcesses();
            }

            isRunning.set(false);
            monitorThread.join(50);//等你50ms,怕我执行太快你还没更新完
            //查看执行结果数据
            InspectExecResponse inspectExecResponse = dockerClient.inspectExecCmd(execId).exec();
            Long exitCodeLong = inspectExecResponse.getExitCodeLong();
            Integer exitCode = (exitCodeLong == null) ? -1 : Math.toIntExact(exitCodeLong);
            ExecuteMessage executeMessage = ExecuteMessage.builder().errMessage(err.toString().trim())
                    .output(out.toString().trim())
                    .exitCode(exitCode == null ? -1L : exitCode)//超时、错误等导致他没有返回结果时，他会为null，但这个可以先判断completion来判断
                    .Timeout(!completion)
                    .time((int) cputimeUsed)
                    .memory((int) (maxMemory.get() / 1024 / 1024))
                    .build();
            inputStream.close();
            return executeMessage;
        } catch (RuntimeException | IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        } finally {
            isRunning.set(false);
        }
    }


    private void restartContainer(){
        dockerClient.restartContainerCmd(containerId).withTimeout(0).exec();

    }


    private String[] getMemPath(){
        String v1Path = "/sys/fs/cgroup/memory/docker/" + fullContainerId + "/memory.usage_in_bytes";
        if (new File(v1Path).exists()) {
            return new String[]{v1Path,"1"};
        }
        else {
            String v2Path = "/sys/fs/cgroup/system.slice/docker-" + fullContainerId + ".scope/memory.current";
            return new String[]{v2Path,"2"};
        }
    }
    private long getCpuTimeTOSeconds() {
        if (type.equals("1")) {//对应v1Path，文件的单位是ns
            String v1Path = "/sys/fs/cgroup/cpuacct/docker/" + fullContainerId + "/cpuacct.usage";
            long time = 0;
            try {
                String content = Files.readString(Paths.get(v1Path)).trim();
                time = Long.parseLong(content);
            } catch (IOException e) {

            }
            return time / 1_000_000;
        }
        //对应v2Path，文件默认单位是微秒
        else {
            String v2Path = "/sys/fs/cgroup/system.slice/docker-" + fullContainerId + ".scope/cpu.stat";
            long time = 0;
            List<String> lines = null;
            try {
                lines = Files.readAllLines(Paths.get(v2Path));
            } catch (IOException e) {

            }
            for (String line : lines) {
                if (line.startsWith("usage_usec")) {
                    // 格式示例: usage_usec 123456
                    String value = line.split("\\s+")[1];
                    // V2 是微秒，除以 1000 得到毫秒
                    return Long.parseLong(value) / 1_000;
                }
            }
            return time;
        }
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