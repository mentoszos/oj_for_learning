package com.codecollab.oj.service;

import com.codecollab.oj.model.dto.QuestionUsecaseAddRequest;
import com.codecollab.oj.model.dto.QuestionUsecaseQueryRequest;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.UsecaseVO;

import java.util.List;

/**
* @author jack li
* @description 针对表【question_usecase(题目测试用例)】的数据库操作Service
* @createDate 2025-12-23 16:43:35
*/
public interface QuestionUsecaseService extends IService<QuestionUsecase> {

    boolean saveBatchUsecase(List<QuestionUsecaseAddRequest> questionAddRequestList);

    PageVO<List<UsecaseVO>> getPage(QuestionUsecaseQueryRequest queryRequest);

    String getInput(Long id);
     String getOutput(Long id);
}
