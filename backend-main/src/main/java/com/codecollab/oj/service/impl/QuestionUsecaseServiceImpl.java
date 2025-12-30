package com.codecollab.oj.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.mapper.QuestionMapper;
import com.codecollab.oj.model.dto.QuestionUsecaseAddRequest;
import com.codecollab.oj.model.dto.QuestionUsecaseQueryRequest;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.UsecaseVO;
import com.codecollab.oj.service.QuestionService;
import com.codecollab.oj.service.QuestionUsecaseService;
import com.codecollab.oj.mapper.QuestionUsecaseMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author jack li
* @description 针对表【question_usecase(题目测试用例)】的数据库操作Service实现
* @createDate 2025-12-23 16:43:35
*/
@Service
public class QuestionUsecaseServiceImpl extends ServiceImpl<QuestionUsecaseMapper, QuestionUsecase> implements QuestionUsecaseService{
    private QuestionMapper questionMapper;

    public QuestionUsecaseServiceImpl(QuestionMapper questionMapper){this.questionMapper = questionMapper;}

    @Override
    @Transactional
    public boolean saveBatchUsecase(List<QuestionUsecaseAddRequest> questionAddRequestList) {
        Integer questionId = questionAddRequestList.get(0).getQuestionId();
        LambdaQueryWrapper<QuestionUsecase> getCount = new LambdaQueryWrapper();
        getCount.eq(QuestionUsecase::getQuestionId,questionId);
        int index = (int) this.count(getCount);//index字段用于更新number,index从0开始
        int activeCount =0;//统计插入中有几个是启用的，用于更新usecase_count

        List<QuestionUsecase> collect = new LinkedList<>();
        for (QuestionUsecaseAddRequest oneRequest:questionAddRequestList){
            if (BooleanUtil.isTrue(oneRequest.getActive())) activeCount++;
            QuestionUsecase questionUsecase = BeanUtil.copyProperties(oneRequest, QuestionUsecase.class);
            questionUsecase.setNumber(index++);
            collect.add(questionUsecase);
        }

        //插入后把usecasecount也更新
        boolean b = this.saveBatch(collect);

        questionMapper.addUsecaseCount(questionId,activeCount);

        return b;
    }

    @Override
    public PageVO<List<UsecaseVO>> getPage(QuestionUsecaseQueryRequest queryRequest) {
        Page<QuestionUsecase> page = new Page<>();
        page.setCurrent(queryRequest.getCurrentPage());
        page.setSize(queryRequest.getPageSize());

        LambdaQueryWrapper<QuestionUsecase> queryWrapper = new LambdaQueryWrapper();
        queryWrapper
                .select(QuestionUsecase::getId,QuestionUsecase::getActive,QuestionUsecase::getNumber,QuestionUsecase::getTimeLimit,QuestionUsecase::getMemoryLimit)
                .eq(QuestionUsecase::getQuestionId,queryRequest.getQuestionId())
                .orderByAsc(QuestionUsecase::getNumber);
        page = this.page(page, queryWrapper);
        PageVO pageVO = PageVO.builder()
                .currentPage(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .totalPages(page.getPages())
                .build();

        List<UsecaseVO> collect = new LinkedList<>();
        pageVO.setRecords(collect);
        int sortNum =1;
        for (QuestionUsecase usecase:page.getRecords()){
            UsecaseVO usecaseVO = BeanUtil.copyProperties(usecase, UsecaseVO.class);
            usecaseVO.setSortNum(sortNum++);
            collect.add(usecaseVO);
        }

        return pageVO;

    }

    @Override
    public String getInput(Long id) {
        LambdaQueryWrapper<QuestionUsecase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(QuestionUsecase::getInput)
                .eq(QuestionUsecase::getId,id);
        QuestionUsecase usecase = this.getOne(queryWrapper);
        return usecase.getInput();
    }

    @Override
    public String getOutput(Long id) {
        LambdaQueryWrapper<QuestionUsecase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(QuestionUsecase::getOutput)
                .eq(QuestionUsecase::getId,id);
        QuestionUsecase usecase = this.getOne(queryWrapper);
        return usecase.getOutput();
    }


}




