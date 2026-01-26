package com.codecollab.oj.Filters;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.codecollab.oj.caches.UserPermsCache;
import com.codecollab.oj.context.UserHolder;
import com.codecollab.oj.mapper.SysMenuMapper;
import com.codecollab.oj.util.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private UserPermsCache userPermsCache;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token  = request.getHeader("Authorization");
        if(StrUtil.isBlank(token)){
            filterChain.doFilter(request,response);
            return;
        }
        if (JWTUtils.validate(token)){
            JSONObject payload = JWTUtils.parse(token);
            String username = payload.getStr("username");
            Integer userId = payload.getInt("userId");
            UserHolder.setUserId(userId);

//            List<String> perms = sysMenuMapper.selectByUserId(userId);
            List<String> perms = userPermsCache.getPerms(userId);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            for(String perm:perms){
                SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority(perm);
                authorities.add(grantedAuthority);
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,null,authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request,response);
    }
}
