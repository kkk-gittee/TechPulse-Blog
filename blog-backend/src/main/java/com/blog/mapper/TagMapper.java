package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("SELECT * FROM tag WHERE name = #{name}")
    Tag selectByName(@Param("name") String name);

    @Update("UPDATE tag SET use_count = use_count + 1 WHERE id = #{tagId}")
    int incrementUseCount(@Param("tagId") Long tagId);
}
