package com.codecollab.oj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.entity.QuestionInfo;
import com.codecollab.oj.service.QuestionInfoService;
import com.codecollab.oj.mapper.QuestionInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【question_info(题目详情)】的数据库操作Service实现
* @createDate 2025-12-23 16:41:26
*/
@Service
public class QuestionInfoServiceImpl extends ServiceImpl<QuestionInfoMapper, QuestionInfo>
    implements QuestionInfoService{

    @Override
    public QuestionInfo getByQuestionId(Integer questionId) {
        LambdaQueryWrapper<QuestionInfo> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(QuestionInfo::getQuestionId,questionId);
        QuestionInfo questionInfo = this.getOne(queryWrapper);
        return questionInfo;
    }
}




