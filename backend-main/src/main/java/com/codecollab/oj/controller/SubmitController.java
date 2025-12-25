package com.codecollab.oj.controller;

import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.constants.ErrorCode;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代码提交控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/submit")
@CrossOrigin(origins = "*")
public class SubmitController {

    @Autowired
    private JudgeService judgeService;

    @PostMapping
    public BaseResponse<SubmitResultVO> submitCode(@RequestBody SubmitRequest request) {
        // TODO: 从请求中获取userId（实际应该从JWT token中获取）
        Long userId = 1L;

        QuestionSubmit submit = judgeService.submitCode(
            request.getProblemId(),
            request.getCode(),
            request.getLanguage(),
            userId
        );

        SubmitResultVO vo = new SubmitResultVO();
        BeanUtils.copyProperties(submit, vo);



        return BaseResponse.success(vo);
    }

    @GetMapping("/{submitId}")
    public BaseResponse<SubmitResultVO> getSubmitResult(@PathVariable Long submitId) {
        QuestionSubmit submit = judgeService.getSubmitResult(submitId);
        if (submit == null) {
            return BaseResponse.error(ErrorCode.NOT_FOUND_ERROR,"提交记录不存在");
        }

        SubmitResultVO vo = new SubmitResultVO();
        BeanUtils.copyProperties(submit, vo);



        return BaseResponse.success(vo);
    }
}

