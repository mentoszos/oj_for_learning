package com.codecollab.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.service.QuestionService;
import com.codecollab.oj.mapper.QuestionMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【question(题目表)】的数据库操作Service实现
* @createDate 2025-12-23 16:42:55
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

}




