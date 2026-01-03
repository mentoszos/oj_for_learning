package com.codecollab.oj.sanbox.controller;

import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.CodeSandbox;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/sandbox")
public class SandboxController {

    @Resource
    private CodeSandbox codeSandbox;

    @PostMapping("/execute")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {

            ExecuteCodeResponse response = codeSandbox.executeCode(executeCodeRequest);
            return response;

    }
}

