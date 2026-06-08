package com.petcare.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.user.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
