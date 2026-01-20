package com.codecollab.oj.controller;

import com.codecollab.oj.caches.UserPermsCache;
import com.codecollab.oj.common.BaseResponse;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.mapper.SysMenuMapper;
import com.codecollab.oj.model.dto.LoginDTO;
import com.codecollab.oj.model.dto.UserInfoDTO;
import com.codecollab.oj.model.entity.LoginUserDetail;
import com.codecollab.oj.util.JWTUtils;
import jakarta.annotation.security.DenyAll;
import jakarta.ws.rs.GET;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private SysMenuMapper sysMenuMapper;

    @Autowired
    private UserPermsCache userPermsCache;
    public AuthController(AuthenticationManager authenticationManager,SysMenuMapper sysMenuMapper){
        this.authenticationManager = authenticationManager;
        this.sysMenuMapper = sysMenuMapper;
    }
    @PostMapping("/login")
    public BaseResponse<String> login(@RequestBody LoginDTO loginDTO){
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        LoginUserDetail userDetail = (LoginUserDetail) authentication.getPrincipal();
        String token = JWTUtils.createToken(Math.toIntExact(userDetail.getUserId()),userDetail.getUsername());
        log.info("登录成功");
        return BaseResponse.success(token);
    }

    @GetMapping("/info")
    public BaseResponse<UserInfoDTO> userInfo(){
        Integer userId = UserHolder.getUserId();
//        List<String> perms = sysMenuMapper.selectByUserId(userId);
        List<String> perms = userPermsCache.getPerms(userId);
        return BaseResponse.success(UserInfoDTO.builder().id(userId).perms(perms).build());
    }
}
