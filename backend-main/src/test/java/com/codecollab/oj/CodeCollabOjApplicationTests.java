package com.codecollab.oj;

import com.codecollab.oj.common.enums.SubmitLanguageType;

import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.JudgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
public class CodeCollabOjApplicationTests {
    @Autowired
    private JudgeService judgeService;
    @Test
    void contextLoads() throws InterruptedException {
        SubmitRequest submitRequest = new SubmitRequest();
        submitRequest.setCode("import java.util.Scanner;\n" +
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
        submitRequest.setSubmitLanguageType(SubmitLanguageType.JAVA);
        submitRequest.setQuestionId(12);

        SubmitResultVO submitResultVO = judgeService.submitCode(submitRequest);
        System.out.println(submitResultVO);




    }

}


