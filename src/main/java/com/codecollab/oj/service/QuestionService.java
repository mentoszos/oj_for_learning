package com.codecollab.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.oj.model.dto.QuestionAddRequest;
import com.codecollab.oj.model.entity.Question;

public interface QuestionService extends IService<Question> {
    Long addQuestion(QuestionAddRequest questionAddRequest);
}
