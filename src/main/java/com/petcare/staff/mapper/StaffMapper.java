package com.petcare.staff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.staff.entity.Staff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StaffMapper extends BaseMapper<Staff> {

    @Select("SELECT * FROM staff WHERE id = #{id} FOR UPDATE")
    Staff selectStaffForUpdate(@Param("id") Long id);
}
