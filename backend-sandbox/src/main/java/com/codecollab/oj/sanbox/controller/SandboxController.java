package com.codecollab.oj.sanbox.controller;

import com.codecollab.oj.model.dto.ExecuteCodeRequest;
import com.codecollab.oj.model.dto.ExecuteCodeResponse;
import com.codecollab.oj.sanbox.CodeSandbox;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sandbox")
public class SandboxController {

    @Resource
    private CodeSandbox codeSandbox;

    @PostMapping("/execute")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        return codeSandbox.executeCode(executeCodeRequest);
    }

    /**
     * 容器池满且借阅超时时抛出 IllegalStateException，此处返回 503 便于主服务 nack 重试。
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handlePoolBusy(IllegalStateException e) {
        if (e.getMessage() != null && e.getMessage().contains("获取容器超时")) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
        throw e;
    }
}

