package com.codecollab.oj.service;

import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.vo.SubmitResultVO;

public interface JudgeService {


    /**
     * 获取判题结果
     */

    SubmitResultVO submitCode(SubmitRequest request);

    QuestionSubmit getSubmitResult(Integer questionId, Integer userId);
}

