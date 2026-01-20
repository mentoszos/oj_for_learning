package com.codecollab.oj.mapper;

import com.codecollab.oj.model.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
* @author jack li
* @description 针对表【sys_user】的数据库操作Mapper
* @createDate 2026-01-20 14:04:36
* @Entity com.codecollab.oj.model.entity.SysUser
*/
public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("select * from sys_user where username = #{username}")
    SysUser selectByUsername(String username);
}




