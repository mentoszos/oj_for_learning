package com.codecollab.oj.Handler;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.common.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UnAuthorizedHandler implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        BaseResponse<Object> error = BaseResponse.error(ErrorCode.NO_AUTH_ERROR);
        response.getWriter().write(JSONUtil.toJsonPrettyStr(error));
    }
}
