package com.codecollab.oj.sanbox;

import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.impl.DockerCodeSandbox;
import com.codecollab.oj.sanbox.pool.ContainerPool;
import com.github.dockerjava.api.DockerClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.InputStream;
import java.util.ArrayList;


@SpringBootTest
class BackendSanboxApplicationTests {
    @Autowired
    private CodeSandbox codeSandbox;
    @Test
    void contextLoads() throws InterruptedException {

        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setCode("import java.util.Scanner;\n" +
                "import java.lang.*;\n" +
                "import java.io.File;\n" +
                "import java.io.IOException;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        // 原有内容：读取输入\n" +
                "        int a = in.nextInt();\n" +
                "        int b = in.nextInt();\n" +
                "\n" +
                "        // --- 新增：生成标记文件 ---\n" +
                "        try {\n" +
                "            // 在当前目录下创建一个名为 \"input_success.txt\" 的空文件\n" +
                "            File file = new File(\"input_success.txt\");\n" +
                "            file.createNewFile();\n" +
                "        } catch (IOException e) {\n" +
                "            e.printStackTrace();\n" +
                "        }\n" +
                "        // --- 新增结束 ---\n" +
                "\n" +
                "        // 原有内容：输出结果\n" +
                "        System.out.println(a + b);\n" +
                "System.exit(0);"+
                "    }\n" +
                "}");

        executeCodeRequest.setLanguageType(SubmitLanguageType.JAVA);
        ArrayList<String>input = new ArrayList<>();
        ArrayList<String>output = new ArrayList<>();
        ArrayList<Long>timelimit = new ArrayList<>();
        input.add("2 2\n");
        output.add("4");
        timelimit.add(1000L);
        executeCodeRequest.setInputs(input);
        executeCodeRequest.setOutputs(output);
        executeCodeRequest.setTimeLimits(timelimit);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

}


