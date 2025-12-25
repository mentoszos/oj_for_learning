package com.codecollab.oj.service;

import com.codecollab.oj.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.oj.model.vo.SubmitResultVO;

/**
* @author jack li
* @description 针对表【question_submit(题目提交记录)】的数据库操作Service
* @createDate 2025-12-23 16:43:31
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    SubmitResultVO getSubmitResult(Integer questionId, Integer userId);
}
