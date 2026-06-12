package com.petcare.staff.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.staff.entity.StaffSkill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StaffSkillMapper extends BaseMapper<StaffSkill> {

    @Select("SELECT * FROM staff_skill WHERE staff_id = #{staffId} AND service_category_id = #{categoryId} FOR UPDATE")
    StaffSkill selectStaffSkillForUpdate(@Param("staffId") Long staffId, @Param("categoryId") Long categoryId);
}
