package com.codecollab.oj.mapper;

import com.codecollab.oj.model.entity.Question;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
* @author jack li
* @description 针对表【question(题目表)】的数据库操作Mapper
* @createDate 2025-12-23 16:42:55
* @Entity com.codecollab.oj.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    @Update("update question set usecase_count =usecase_count+#{activeCount} WHERE id = #{id}")
    void addUsecaseCount(@Param("id")Integer id, @Param("activeCount") Integer activeCount);
}




