package com.codecollab.oj.model.dto;

import lombok.Data;
import org.intellij.lang.annotations.Language;

import java.io.Serializable;

/**
 * 代码提交请求
 */
@Data
public class SubmitRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String questionId;
    private String code;
    private String language;

    //{questionId: 12,…}
    //code
    //:
    //"public class Main {\n    public static void main(String[] args) {\n        Scanner scanner = new Scanner(System.in);\n        int a = scanner.nextInt();\n        int b = scanner.nextInt();\n        System.out.println(a + b);\n        // 在这里编写你的代码\n    }\n}"
    //language
    //:
    //"java"
    //questionId
    //:
    //12
}

