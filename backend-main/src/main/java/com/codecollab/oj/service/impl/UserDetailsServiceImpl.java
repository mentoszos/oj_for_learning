package com.codecollab.oj.service.impl;

import cn.hutool.core.util.StrUtil;
import com.codecollab.oj.caches.UserPermsCache;
import com.codecollab.oj.mapper.SysMenuMapper;
import com.codecollab.oj.mapper.SysUserMapper;
import com.codecollab.oj.model.entity.LoginUserDetail;
import com.codecollab.oj.model.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private UserPermsCache userPermsCache;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectByUsername(username);
        if (sysUser == null) throw new UsernameNotFoundException("用户不存在");
        List<String> perms = sysMenuMapper.selectByUserId(Math.toIntExact(sysUser.getId()));
        userPermsCache.addPerms(Math.toIntExact(sysUser.getId()),perms);
        List<SimpleGrantedAuthority> authorities = perms.stream().filter(StrUtil::isNotBlank).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return new LoginUserDetail(sysUser.getId(),sysUser.getUsername(), sysUser.getPassword(), authorities);
    }
}
