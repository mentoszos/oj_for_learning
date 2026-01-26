package com.codecollab.oj.controller;

import cn.hutool.core.util.ObjectUtil;
import com.codecollab.oj.annotations.RateLimit;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.enums.ErrorCode;

import com.codecollab.oj.common.enums.SubmitLanguageType;
import com.codecollab.oj.model.dto.DebugRequest;
import com.codecollab.oj.model.dto.DeleteRequest;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.QuestionSubmit;

import com.codecollab.oj.model.vo.DebugVO;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.JudgeService;
import com.codecollab.oj.service.QuestionSubmitService;
import com.codecollab.oj.util.ThrowUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Produces;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 代码提交控制器
 */
@RestController
@RequestMapping("/submission")
@Slf4j
@Tag(name = "题目提交模块",description = "题目提交")
public class SubmitController {

    private QuestionSubmitService questionSubmitService;
    private JudgeService judgeService;
    public SubmitController(JudgeService judgeService,QuestionSubmitService questionSubmitService){
        this.judgeService = judgeService;
        this.questionSubmitService = questionSubmitService;
    }
/// /////////////////////////////////////////////////////////////////////////////
//    @PostMapping(value = "/submit")
//    @Operation(summary = "提交代码并判题")
//    public BaseResponse<SubmitResultVO> submitCode(@RequestBody SubmitRequest request) {
//        String code = request.getCode();
//        Integer questionId = request.getQuestionId();
//        SubmitLanguageType submitLanguageType = request.getSubmitLanguageType();
//        QuestionSubmit questionSubmit = QuestionSubmit.builder().sumbitCode(code).questionId(questionId).status(0).codeLanguage(submitLanguageType).userId(1).build();
//        questionSubmitService.save(questionSubmit);
//        Long id = questionSubmit.getId();
//        SubmitResultVO submitResultVO = SubmitResultVO.builder().id(id).build();
//        return BaseResponse.success(submitResultVO);
//    }
//    @GetMapping(value = "/submitResult",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    @Operation(summary = "sse推送判题结果")
//    public SseEmitter sse(@RequestParam(name = "submitId") Long id) {
//        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
//        SubmitRequest submitRequest = new SubmitRequest();
//        submitRequest.setQuestionId(questionSubmit.getQuestionId());
//        submitRequest.setCode(questionSubmit.getSumbitCode());
//        submitRequest.setSubmitLanguageType(questionSubmit.getCodeLanguage());
//        SseEmitter emitter = new SseEmitter(15_000L);
//        emitter.onTimeout(()->emitter.complete());
//        judgeService.submitCodeAsync(submitRequest,emitter);
//        return emitter;
//    }
/// ///////////////////////////////////////////////////////////////
    @PostMapping(value = "/submit")
    @Operation(summary = "提交代码并判题")
    @PreAuthorize("hasAuthority('submission:add')")
    @RateLimit
    public BaseResponse<SubmitResultVO> submitCode(@RequestBody SubmitRequest request) {
        SubmitResultVO submitResultVO = judgeService.submitCode(request);
        return BaseResponse.success(submitResultVO);
    }

    @PostMapping("/debug")
    @Operation(summary = "debug代码")
    @PreAuthorize("hasAuthority('submission:add')")
    @RateLimit
    public BaseResponse<DebugVO> debugCode(@RequestBody DebugRequest request) {
        DebugVO vo = judgeService.debugCode(request);
        return BaseResponse.success(vo);
    }


    @GetMapping
    @Operation(summary = "查询用户某一题的提交记录")
    @PreAuthorize("hasAuthority('submission:view')")
    @RateLimit
    public BaseResponse<SubmitResultVO> submitResult(@RequestParam Integer questionId, @RequestParam Integer userId){
        ThrowUtils.throwIf(!(ObjectUtil.isNotEmpty(questionId) && ObjectUtil.isNotEmpty(userId)), ErrorCode.NULL_ERROR,"查询用户的提交记录，参数不全");
        SubmitResultVO resultVo= questionSubmitService.getSubmitResult(questionId,userId);
        return BaseResponse.success(resultVo);
    }
//    @DeleteMapping("/submitRecord")
//    @Operation(summary = "删除某一个提交记录")
//    @RateLimit
//    public BaseResponse<?> delSubmitResult(@RequestParam Long id){
//        boolean b = questionSubmitService.removeById(id);
//        if (b) return BaseResponse.success(b);
//        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"删除失败");
//    }
    @PostMapping("/submitRecord/batch")
    @Operation(summary = "批量删除提交记录")
    @PreAuthorize("hasAuthority('submission:manage')")
    @RateLimit
    public BaseResponse<?> delBatchSubmitResult(@RequestBody DeleteRequest deleteRequest){
        List<Long> ids = deleteRequest.getIds();
        boolean b = questionSubmitService.removeBatchByIds(ids);
        if (b) return BaseResponse.success(b);
        return BaseResponse.error(ErrorCode.OPERATION_ERROR,"删除失败");
    }
}

