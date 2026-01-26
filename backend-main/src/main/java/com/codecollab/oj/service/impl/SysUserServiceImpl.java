package com.codecollab.oj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codecollab.oj.model.entity.SysUser;
import com.codecollab.oj.service.SysUserService;
import com.codecollab.oj.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
* @author jack li
* @description 针对表【sys_user】的数据库操作Service实现
* @createDate 2026-01-20 14:04:36
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper,SysUser>
    implements SysUserService{

}




