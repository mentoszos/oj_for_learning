package com.codecollab.oj.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.oj.model.entity.SysMenu;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {
    @Select("SELECT DISTINCT sm.perms\n" +
            "FROM sys_menu sm\n" +
            "    -- 1. 关联角色菜单关系表\n" +
            "    INNER JOIN sys_role_menu srm ON sm.id = srm.menu_id\n" +
            "    -- 2. 关联用户角色关系表\n" +
            "    INNER JOIN sys_user_role sur ON srm.role_id = sur.role_id\n" +
            "WHERE sur.user_id = #{userId}\n" +
            "  AND sm.perms IS NOT NULL \n" +
            "  AND sm.perms <> ''")
    List<String> selectByUserId(Integer userId);
}




