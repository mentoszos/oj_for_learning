package com.codecollab.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.codecollab.oj.service.QuestionUsecaseService;
import com.codecollab.oj.mapper.QuestionUsecaseMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【question_usecase(题目测试用例)】的数据库操作Service实现
* @createDate 2025-12-23 16:43:35
*/
@Service
public class QuestionUsecaseServiceImpl extends ServiceImpl<QuestionUsecaseMapper, QuestionUsecase>
    implements QuestionUsecaseService{

}




