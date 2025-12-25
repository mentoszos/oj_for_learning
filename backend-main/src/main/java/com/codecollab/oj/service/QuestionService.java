package com.codecollab.oj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.oj.model.dto.QuestionAddRequest;
import com.codecollab.oj.model.dto.QuestionModifyRequest;
import com.codecollab.oj.model.dto.QuestionQueryRequest;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.QuestionVO;

import java.util.List;

public interface QuestionService extends IService<Question> {
    boolean addQuestion(QuestionAddRequest questionAddRequest);

    QuestionVO getQuestionById(Integer id);

    boolean removeQuestionsByIds(List<Long> ids);

    boolean removeQuestionsById(Long id);

    PageVO<QuestionVO> getQuestions(QuestionQueryRequest questionQueryRequest);

    boolean modify(QuestionModifyRequest questionModifyRequest);
}
