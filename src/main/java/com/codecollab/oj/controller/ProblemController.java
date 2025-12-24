package com.codecollab.oj.controller;

import cn.hutool.core.bean.BeanUtil;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.model.dto.DeleteRequest;
import com.codecollab.oj.model.dto.QuestionAddRequest;
import com.codecollab.oj.model.entity.Question;
import com.codecollab.oj.model.vo.QuestionVO;
import com.codecollab.oj.service.QuestionService;
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
    public BaseResponse getQuestion(@PathVariable Long id){

    }

    @DeleteMapping("/batch")
    @Operation(description = "批量根据id删")
    public BaseResponse delQuestion(@RequestBody DeleteRequest deleteRequest){
        List<Long> ids = deleteRequest.getIds();
        questionService.removeBatchByIds(ids);
        return BaseResponse.success();
    }
    @PostMapping
    public BaseResponse addQuestion(@RequestBody QuestionAddRequest questionAddRequest){
        Long questionId = questionService.addQuestion(questionAddRequest);
        return BaseResponse.success(questionId);
    }

}
