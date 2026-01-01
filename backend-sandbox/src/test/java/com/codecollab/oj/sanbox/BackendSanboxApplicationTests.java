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
        executeCodeRequest.setCode("import java.util.Scanner;\n" +
                "\n" +
                "public class Main {\n" +
                "    public static void main(String[] args){\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "\n" +
                "        int a = in.nextInt();\n" +
                "        int b = in.nextInt();\n" +
                "        \n" +
                "        System.out.println(a + b);\n" +
                "\n" +

                "    }\n" +
                "}\n");

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
        executeCodeRequest.setOutputs(output);
        executeCodeRequest.setTimeLimits(timelimit);
        executeCodeRequest.setMemoryLimits(memorylimit);
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

}


