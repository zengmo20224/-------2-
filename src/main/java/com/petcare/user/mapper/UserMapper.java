package com.petcare.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM `user` WHERE id = #{id} AND status = 'ACTIVE' AND deleted = 0 FOR UPDATE")
    User selectActiveForUpdate(@Param("id") Long id);
}
