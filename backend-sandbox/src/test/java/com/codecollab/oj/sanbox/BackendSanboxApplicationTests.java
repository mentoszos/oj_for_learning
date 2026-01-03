package com.codecollab.oj.sanbox;

import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.common.enums.SubmitStatus;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.ArrayList;


@SpringBootTest
class BackendSanboxApplicationTests {
    @Autowired
    private CodeSandbox codeSandbox;
    @Test
    void contextLoads() throws InterruptedException {

        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setCode("import java.util.Scanner;\nimport java.io.FileWriter;\nimport java.io.IOException;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner in = new Scanner(System.in);\n        int a = 0;\n        int b = 0;\n\n        // 尝试读取输入，如果读不到则保持默认值 0\n        if (in.hasNextInt()) {\n            a = in.nextInt();\n        }\n        if (in.hasNextInt()) {\n            b = in.nextInt();\n        }\n\n        // 标准输出打印结果\n        System.out.println(a + b);\n\n        // --- 调试核心：将结果写入文件 ---\n        try (FileWriter writer = new FileWriter(\"debug.txt\")) {\n            writer.write(\"Received a: \" + a + \"\\n\");\n            writer.write(\"Received b: \" + b + \"\\n\");\n            writer.flush();\n        } catch (IOException e) {\n            // 如果写入文件失败，打印到标准错误流\n            System.err.println(\"File write failed: \" + e.getMessage());\n        }\n    }\n}");

        executeCodeRequest.setLanguageType(SubmitLanguageType.JAVA);
        ArrayList<String>input = new ArrayList<>();
        ArrayList<String>output = new ArrayList<>();
        ArrayList<Long>timelimit = new ArrayList<>();
        ArrayList<Double>memorylimit = new ArrayList<>();
        input.add("4 2\n");
        output.add("6");
        timelimit.add(1000L);
        memorylimit.add(256.0);
        executeCodeRequest.setInputs(input);

        executeCodeRequest.setTimeLimits(timelimit);
        executeCodeRequest.setMemoryLimits(memorylimit);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

}


