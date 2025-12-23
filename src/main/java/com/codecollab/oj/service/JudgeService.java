package com.codecollab.oj.service;

import com.codecollab.oj.model.entity.QuestionSubmit;

public interface JudgeService {
    /**
     * 提交代码进行判题
     */
    QuestionSubmit submitCode(Long questionId, String code, String language, Long userId);

    /**
     * 获取判题结果
     */
    QuestionSubmit getSubmitResult(Long submitId);
}

