package com.codecollab.oj.service;

import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.vo.SubmitResultVO;

public interface JudgeService {


    /**
     * 获取判题结果
     */
    QuestionSubmit getSubmitResult(Long submitId);

    SubmitResultVO submitCode(SubmitRequest request);
}

