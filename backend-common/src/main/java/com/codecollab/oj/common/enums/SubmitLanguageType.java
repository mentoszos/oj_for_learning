package com.codecollab.oj.common.enums;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SubmitLanguageType {
    JAVA("Java", "java"),
    CPLUSPLUS("C++", "cpp"),
    C("C", "c"),
    PYTHON("Python", "python"),
    GOLANG("Go", "go"),
    JAVASCRIPT("JavaScript", "javascript");

    private final String text;
    @JsonValue
    private final String value;

    SubmitLanguageType(String text, String value) {
        this.text = text;
        this.value = value;
    }

    //获取值列表

    public static List<String> getValues(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }

    //根据value获取枚举
    public static SubmitLanguageType getEnumByValue(String value){
        if (ObjectUtil.isEmpty(value)) return null;

        for (SubmitLanguageType typeEnum: SubmitLanguageType.values()){
            if (StrUtil.equals(typeEnum.value,value)) return typeEnum;
        }
        return null;
    }
}
