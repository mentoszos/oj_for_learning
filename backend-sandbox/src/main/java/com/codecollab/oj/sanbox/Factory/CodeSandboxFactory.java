package com.codecollab.oj.sanbox.Factory;

import com.codecollab.oj.sanbox.CodeSandbox;
import com.codecollab.oj.sanbox.impl.DockerCodeSandbox;
import com.google.common.base.CaseFormat;
import org.springframework.stereotype.Component;

@Component
public class CodeSandboxFactory {
    //todo 这个在懒加载中需要有线程安全的操作
    public static CodeSandbox newInstance(String type){
        switch (type){
            case "docker": return new DockerCodeSandbox();
            default:return new DockerCodeSandbox();
        }
    }
}
