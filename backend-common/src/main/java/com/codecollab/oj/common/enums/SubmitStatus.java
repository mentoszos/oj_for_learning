package com.codecollab.oj.common.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SubmitStatus {
    ACCEPTED("accepted",1),
    WA("WrongAnswer",0),
    TLE("TimeLimitExceed",2),
    MLE("MemoryLimitExceed",3),
    RE("RuntimeError",4),
    CE("CompileError",5),
    ERROR("DockerError",7),
    OLE("OutputLimitExceed",6);
    private final String text;
    private final Integer value;

    SubmitStatus(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    //获取值列表
    public static List<Integer> getValues(){
        return Arrays.stream(values()).map(item->item.value).collect(Collectors.toList());
    }

    //根据value获取枚举
    public static SubmitStatus getEnumByValue(Integer value){
        if (ObjectUtil.isEmpty(value)) return null;

        for (SubmitStatus statusEnum: SubmitStatus.values()){
            if (statusEnum.value.equals(value)) return statusEnum;
        }
        return null;
    }
}
