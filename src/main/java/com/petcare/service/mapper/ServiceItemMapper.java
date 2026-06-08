package com.petcare.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.service.entity.ServiceItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceItemMapper extends BaseMapper<ServiceItem> {
}
