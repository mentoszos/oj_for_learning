package com.codecollab.oj.controller;

import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.enums.ErrorCode;
import com.codecollab.oj.model.dto.DebugCodeRequest;
import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.entity.QuestionSubmit;
import com.codecollab.oj.model.vo.DebugVO;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.JudgeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代码提交控制器
 */
@RestController
@RequestMapping(("/submission"))
@Slf4j
@Tag(name = "题目提交模块",description = "题目提交")
public class SubmitController {

    private JudgeService judgeService;
    public SubmitController(JudgeService judgeService){
        this.judgeService = judgeService;
    }

    @PostMapping("/submit")
    public BaseResponse<SubmitResultVO> submitCode(@RequestBody SubmitRequest request) {
        judgeService.submitCode(request);

        SubmitResultVO vo = new SubmitResultVO();
        BeanUtils.copyProperties(submit, vo);



        return BaseResponse.success(vo);
    }
    @PostMapping("/debug")
    public BaseResponse<DebugVO>debugCode(@RequestBody DebugCodeRequest debugCodeRequest){
        DebugVO debugVO = judgeService.debugCode(debugCodeRequest);
        return BaseResponse.success(debugVO);
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

