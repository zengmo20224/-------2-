package com.petcare.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.booking.entity.ServiceBooking;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ServiceBookingMapper extends BaseMapper<ServiceBooking> {
}
