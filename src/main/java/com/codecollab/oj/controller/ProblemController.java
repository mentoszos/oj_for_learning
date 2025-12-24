package com.codecollab.oj.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.constants.ErrorCode;
import com.codecollab.oj.model.dto.*;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.entity.QuestionInfo;
import com.codecollab.oj.model.entity.QuestionUsecase;
import com.codecollab.oj.model.vo.PageVO;
import com.codecollab.oj.model.vo.QuestionVO;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.model.vo.UsecaseVO;
import com.codecollab.oj.service.QuestionInfoService;
import com.codecollab.oj.service.QuestionService;
import com.codecollab.oj.service.QuestionSubmitService;
import com.codecollab.oj.service.QuestionUsecaseService;
import com.codecollab.oj.util.ThrowUtils;
import com.fasterxml.jackson.databind.ser.Serializers;
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
    private QuestionInfoService questionInfoService;
    private QuestionUsecaseService questionUsecaseService;
    private QuestionSubmitService questionSubmitService;

    public ProblemController(QuestionService questionService,QuestionInfoService questionInfoService,QuestionUsecaseService questionUsecaseService,QuestionSubmitService questionSubmitService){
        this.questionService = questionService;
        this.questionInfoService = questionInfoService;
        this.questionUsecaseService = questionUsecaseService;
        this.questionSubmitService = questionSubmitService;
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
    @Operation(description = "批量根据ids删")
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

    @GetMapping("/info/{questionId}")
    @Operation(description = "题目内容查询")
    public BaseResponse<QuestionInfo> questionInfo(@PathVariable Integer questionId){
        ThrowUtils.throwIf(ObjectUtil.isNull(questionId),ErrorCode.NULL_ERROR);
        QuestionInfo questionInfo = questionInfoService.getByQuestionId(questionId);
        return BaseResponse.success(questionInfo);
    }

    @GetMapping("/submit")
    @Operation(description = "查询用户某一题的提交记录")
    public BaseResponse<SubmitResultVO> submitResult(@RequestParam Integer questionId, @RequestParam Integer userId){
        ThrowUtils.throwIf(!(ObjectUtil.isNotEmpty(questionId) && ObjectUtil.isNotEmpty(userId)), ErrorCode.NULL_ERROR,"查询用户的提交记录，参数不全");
        SubmitResultVO resultVo= questionSubmitService.getSubmitResult(questionId,userId);
        return BaseResponse.success(resultVo);
    }
    @DeleteMapping("/submit")
    @Operation(description = "删除某一个记录")
    public BaseResponse<?> delSubmitResult(@RequestParam Long id){
        boolean b = questionSubmitService.removeById(id);
        if (b) return BaseResponse.success(b);
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"删除失败");
    }
    @PostMapping("/submit/batch")
    @Operation(description = "批量删除记录")
    public BaseResponse<?> delBatchSubmitResult(@RequestBody DeleteRequest deleteRequest){
        List<Long> ids = deleteRequest.getIds();
        boolean b = questionSubmitService.removeBatchByIds(ids);
        if (b) return BaseResponse.success(b);
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"删除失败");
    }


    /// ////////////////////////////////////////////////////////////////////////////
    @PostMapping("/usecase")
    @Operation(description = "批量增加测试用例")
    public BaseResponse<?> addBatchUsecase(@RequestBody List<QuestionUsecaseAddRequest> questionUsecaseAddRequests){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(questionUsecaseAddRequests),ErrorCode.NULL_ERROR);

        boolean b = questionUsecaseService.saveBatchUsecase(questionUsecaseAddRequests);
        if (b) return BaseResponse.success(b);
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"新增失败");
    }

    @PostMapping(/)



    @PostMapping("/usecase/page")
    @Operation(description = "分页查询测试用例")
    //只会返回id，和number，具体的内容需要调用getUsecase获取
    public BaseResponse<PageVO<List<UsecaseVO>>> usecasePage(@RequestBody QuestionUsecaseQueryRequest queryRequest){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(queryRequest),ErrorCode.NULL_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isEmpty(queryRequest.getQuestionId()),ErrorCode.NULL_ERROR,"需要提供question_id");
        PageVO<List<UsecaseVO>> pageResult = questionUsecaseService.getPage(queryRequest);
        return BaseResponse.success(pageResult);
    }
    @GetMapping("/usecase/input")
    @Operation(description = "查询测试用例的输入")
    //todo
    //后期可以考虑返回文件或者其他形式
    public BaseResponse<String> usecaseInput(@RequestParam Long id){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(id),ErrorCode.NULL_ERROR);
        String inputText = questionUsecaseService.getInput(id);
        return BaseResponse.success(inputText);
    }

    @GetMapping("/usecase/output")
    @Operation(description = "查询测试用例的输出")
    //todo
    //后期可以考虑返回文件或者其他形式
    public BaseResponse<String> usecaseOutput(@RequestParam Long id){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(id),ErrorCode.NULL_ERROR);
        String outputText = questionUsecaseService.getOutput(id);
        return BaseResponse.success(outputText);
    }



    @GetMapping("/usecase")
    @Operation(description = "查询详细的测试用例")
    //这个接口暂时用不到
    public BaseResponse<UsecaseVO> getUsecase(@RequestParam Integer id){
        ThrowUtils.throwIf(ObjectUtil.isEmpty(id),ErrorCode.NULL_ERROR);
        QuestionUsecase usecase = questionUsecaseService.getById(id);
        UsecaseVO usecaseVO = BeanUtil.copyProperties(usecase, UsecaseVO.class);
        return BaseResponse.success(usecaseVO);
    }
}
