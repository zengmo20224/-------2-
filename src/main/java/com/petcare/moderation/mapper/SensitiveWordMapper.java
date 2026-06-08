package com.petcare.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.moderation.entity.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SensitiveWordMapper extends BaseMapper<SensitiveWord> {
}
