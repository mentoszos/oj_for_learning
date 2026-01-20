package com.codecollab.oj;

import com.codecollab.oj.common.enums.SubmitLanguageType;

import com.codecollab.oj.model.dto.SubmitRequest;
import com.codecollab.oj.model.vo.SubmitResultVO;
import com.codecollab.oj.service.JudgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;



@SpringBootTest
public class CodeCollabOjApplicationTests {
    @Autowired
    private JudgeService judgeService;


}


