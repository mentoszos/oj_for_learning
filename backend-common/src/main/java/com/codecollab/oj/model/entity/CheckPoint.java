package com.codecollab.oj.model.entity;

import com.codecollab.oj.common.enums.SubmitStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
/**
 * 这个类代表提交记录中的一个测试用例结果，跟QuestionSubmit类绑定
 */
public class CheckPoint {
    private Boolean accepted; //用例是否通过

    private Double memory; // 使用的内存，MB

    private Integer time; // 耗时，ms

    private SubmitStatus submitStatus;


}
