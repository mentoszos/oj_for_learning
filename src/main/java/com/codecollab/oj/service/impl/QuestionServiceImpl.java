package com.codecollab.oj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.model.dto.*;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.entity.QuestionInfo;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.QuestionVO;
import com.codecollab.oj.service.*;
import com.codecollab.oj.mapper.QuestionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author jack li
* @description 针对表【question(题目表)】的数据库操作Service实现
* @createDate 2025-12-23 16:42:55
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService{
    private QuestionInfoService questionInfoService;
    private QuestionUsecaseService questionUsecaseService;
    private QuestionSolutionService questionSolutionService;
    private QuestionSubmitService questionSubmitService;

    public QuestionServiceImpl(QuestionInfoService questionInfoService, QuestionUsecaseService questionUsecaseService,
                               QuestionSolutionService questionSolutionService,QuestionSubmitService questionSubmitService){
        this.questionInfoService = questionInfoService;
        this.questionUsecaseService = questionUsecaseService;
        this.questionSolutionService = questionSolutionService;
        this.questionSubmitService = questionSubmitService;
    }

    @Override
    @Transactional
    public boolean addQuestion(QuestionAddRequest questionAddRequest) {
        Question question = BeanUtil.copyProperties(questionAddRequest, Question.class);
        question.setSubmitNum(0);
        question.setAcceptedNum(0);
        question.setUserId(UserHolder.getUserId());
        this.save(question);

        Integer questionId = question.getId();
        QuestionInfo questionInfo = new QuestionInfo();
        questionInfo.setContent(questionAddRequest.getContent());
        questionInfo.setQuestionId(questionId);
        questionInfoService.save(questionInfo);

        List<QuestionUsecaseAddRequest> usecases = questionAddRequest.getUsecases();
        List<QuestionUsecase> usecaseList = new LinkedList<>();
        for(QuestionUsecaseAddRequest usecaseAddRequest:usecases){
            QuestionUsecase questionUsecase = BeanUtil.copyProperties(usecaseAddRequest, QuestionUsecase.class);
            questionUsecase.setQuestionId(questionId);
            usecaseList.add(questionUsecase);
        }
        questionUsecaseService.saveBatch(usecaseList);

        return true;
    }

    @Override
    public QuestionVO getQuestionById(Integer id) {
        Question question = this.getById(id);
        QuestionVO questionVO = BeanUtil.copyProperties(question, QuestionVO.class);

        Integer questionId = id;
        LambdaQueryWrapper<QuestionInfo> infoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        infoLambdaQueryWrapper.eq(QuestionInfo::getQuestionId,questionId);
        QuestionInfo questionInfo = questionInfoService.getOne(infoLambdaQueryWrapper);
        questionVO.setContent(questionInfo.getContent());

        return questionVO;
    }

    @Override
    @Transactional
    public boolean removeQuestionsByIds(List<Long> ids) {
        questionInfoService.removeBatchByIds(ids);
        questionUsecaseService.removeBatchByIds(ids);
        questionSolutionService.removeBatchByIds(ids);
        questionSubmitService.removeBatchByIds(ids);
        boolean b = this.removeBatchByIds(ids);
        return b;
    }

    @Override
    public boolean removeQuestionsById(Long id) {
        questionInfoService.removeById(id);
        questionUsecaseService.removeById(id);
        questionSolutionService.removeById(id);
        questionSubmitService.removeById(id);
        return this.removeById(id);
    }

    @Override
    public PageVO<QuestionVO> getQuestions(QuestionQueryRequest questionQueryRequest) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.likeRight(StrUtil.isNotBlank(questionQueryRequest.getTitle()), Question::getTitle, questionQueryRequest.getTitle())
                .apply(CollectionUtil.isNotEmpty(questionQueryRequest.getTags()),"JSON_CONTAINS(tag,{0})", JSONUtil.toJsonStr(questionQueryRequest.getTags()));

        Page<Question> page = new Page<>(questionQueryRequest.getCurrent(),questionQueryRequest.getPageSize());
        Page<Question> pageResult = this.page(page, queryWrapper);

        PageVO<QuestionVO> pageVO = new PageVO<>();
        pageVO.setCurrentPage(pageResult.getCurrent());
        pageVO.setPageSize(pageResult.getSize());
        pageVO.setTotal(pageResult.getTotal());
        pageVO.setTotalPages(pageResult.getPages());
        List<QuestionVO> recordList = pageResult.getRecords().stream().map(question -> BeanUtil.copyProperties(question, QuestionVO.class)).collect(Collectors.toList());
        pageVO.setRecords(recordList);


        return pageVO;
    }

    @Override
    @Transactional
    public boolean modify(QuestionModifyRequest questionModifyRequest) {
        Question question = new Question();
        question.setId(questionModifyRequest.getId());
        question.setTitle(questionModifyRequest.getTitle());
        question.setTags(questionModifyRequest.getTags());
        this.updateById(question);

        List<QuestionUsecaseModifyRequest> usecaseModifyRequestList = questionModifyRequest.getUsecaseModifyRequestList();
        if (CollectionUtil.isEmpty(usecaseModifyRequestList)) return false;
        List<QuestionUsecase> usecases = usecaseModifyRequestList.stream().map(usecaseModifyRequest -> BeanUtil.copyProperties(usecaseModifyRequest, QuestionUsecase.class)).collect(Collectors.toList());
        boolean b = questionUsecaseService.updateBatchById(usecases);

        return b;
    }
}




