package com.codecollab.oj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.QuestionSubmitService;
import com.codecollab.oj.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【question_submit(题目提交记录)】的数据库操作Service实现
* @createDate 2025-12-23 16:43:31
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{

    @Override
    public SubmitResultVO getSubmitResult(Integer questionId, Integer userId) {
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(QuestionSubmit::getQuestionId,questionId)
                .eq(QuestionSubmit::getUserId,userId);
        QuestionSubmit questionSubmit = this.getOne(queryWrapper);
        SubmitResultVO submitResultVO = BeanUtil.copyProperties(questionSubmit, SubmitResultVO.class);
        return submitResultVO;
    }
}




