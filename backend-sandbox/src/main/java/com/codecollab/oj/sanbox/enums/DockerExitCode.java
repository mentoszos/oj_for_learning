package com.codecollab.oj.sanbox.enums;

import cn.hutool.core.util.ObjectUtil;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum DockerExitCode {
    SUCESS("SUCESS",0),
    CE_OR_RE("COMPILE ERROR or RUNTIME ERROR",1),
    MLE("MEMORY LIMIT ERROR",137);


    private final String text;
    private final Integer value;

    DockerExitCode(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    //获取值列表
    public static List<Integer> getValues(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }

    //根据value获取枚举
    public static DockerExitCode getEnumByValue(Long value){
        if (ObjectUtil.isEmpty(value)) return null;

        for (DockerExitCode statusEnum: DockerExitCode.values()){
            if (statusEnum.value.equals(value)) return statusEnum;
        }
        return null;
    }

}
