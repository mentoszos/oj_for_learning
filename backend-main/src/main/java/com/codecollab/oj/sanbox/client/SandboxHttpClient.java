//package com.codecollab.oj.sanbox.client;
//
//import com.alibaba.fastjson2.JSON;
//import com.codecollab.oj.common.BaseResponse;
//import com.codecollab.oj.model.dto.ExecuteCodeRequest;
//import com.codecollab.oj.model.dto.ExecuteCodeResponse;
//import com.codecollab.oj.sanbox.CodeSandbox;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.*;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Primary;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.util.concurrent.TimeUnit;
//
//@Slf4j
//@Component
//@Primary
//public class SandboxHttpClient implements CodeSandbox {
//
//    @Value("${sandbox.url:http://localhost:8801}")
//    private String sandboxUrl;
//
//    private final OkHttpClient httpClient;
//
//    public SandboxHttpClient() {
//        this.httpClient = new OkHttpClient.Builder()
//                .connectTimeout(30, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .build();
//    }
//
//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
//        String url = sandboxUrl + "/sandbox/execute";
//
//        try {
//            String json = JSON.toJSONString(executeCodeRequest);
//            RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .build();
//
//            try (Response response = httpClient.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    log.error("调用sandbox服务失败: {}", response.code());
//                    throw new RuntimeException("调用sandbox服务失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                BaseResponse<?> baseResponse = JSON.parseObject(responseBody, BaseResponse.class);
//                ExecuteCodeResponse data = JSON.parseObject(
//                        JSON.toJSONString(baseResponse.getData()),
//                        ExecuteCodeResponse.class
//                );
//
//                if (baseResponse.getCode() == 200 && data != null) {
//                    return data;
//                } else {
//                    log.error("sandbox服务返回错误: {}", baseResponse.getMessage());
//                    throw new RuntimeException("sandbox服务返回错误: " + baseResponse.getMessage());
//                }
//            }
//        } catch (IOException e) {
//            log.error("调用sandbox服务异常", e);
//            throw new RuntimeException("调用sandbox服务异常: " + e.getMessage(), e);
//        }
//    }
//}
//
