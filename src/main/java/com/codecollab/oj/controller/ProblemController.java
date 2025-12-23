package com.codecollab.oj.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.constants.ErrorCode;
import com.codecollab.oj.model.dto.DeleteRequest;
import com.codecollab.oj.model.dto.QuestionAddRequest;
import com.codecollab.oj.model.dto.QuestionModifyRequest;
import com.codecollab.oj.model.dto.QuestionQueryRequest;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.QuestionVO;
import com.codecollab.oj.service.QuestionService;
import com.codecollab.oj.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(("/questions"))
@Slf4j
@Tag(name = "questions",description = "题目管理")
public class ProblemController {
    private QuestionService questionService;

    public ProblemController(QuestionService questionService){
        this.questionService = questionService;
    }

    @GetMapping("/{id}")
    @Operation(description = "根据id查")
    public BaseResponse<QuestionVO> getQuestion(@PathVariable Integer id){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(id), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        QuestionVO questionVO = questionService.getQuestionById(id);
        return BaseResponse.success(questionVO);
    }

    @GetMapping("/page")
    @Operation(description = "条件查分页")
    public BaseResponse<PageVO<QuestionVO>> getQuestions(QuestionQueryRequest questionQueryRequest){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(questionQueryRequest), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        PageVO<QuestionVO> questions = questionService.getQuestions(questionQueryRequest);
        return BaseResponse.success(questions);
    }

    @PutMapping
    @Operation(description = "修改题目信息")
    public BaseResponse updateQuestion(@RequestBody QuestionModifyRequest questionModifyRequest){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(questionModifyRequest), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        boolean b = questionService.modify(questionModifyRequest);


        if (b)return BaseResponse.success();
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"修改题目信息失败");
    }
    @DeleteMapping("/{id}")
    @Operation(description = "根据id删")
    public BaseResponse delQuestion(@PathVariable Long id){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(id), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        boolean b = questionService.removeQuestionsById(id);
        if(b) return BaseResponse.success();
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"根据id删除题目失败");
    }

    @DeleteMapping("/batch")
    @Operation(description = "批量根据id删")
    public BaseResponse delQuestions(@RequestBody DeleteRequest deleteRequest){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(deleteRequest), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        List<Long> ids = deleteRequest.getIds();
        boolean b = questionService.removeQuestionsByIds(ids);
        if(b) return BaseResponse.success();
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"根据id批量删除题目失败");
    }

    @PostMapping
    public BaseResponse<Integer> addQuestion(@RequestBody QuestionAddRequest questionAddRequest){
        ThrowUtils.throwIf(ObjectUtil.isAllEmpty(questionAddRequest), ErrorCode.NOT_FOUND_ERROR,"参数不能为空");
        log.info("问题添加,题目为：{}",questionAddRequest.getTitle());
        boolean b = questionService.addQuestion(questionAddRequest);
        if(b) return BaseResponse.success();
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"添加题目失败");
    }

}
