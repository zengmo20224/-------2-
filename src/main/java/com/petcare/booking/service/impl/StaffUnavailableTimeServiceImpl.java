package com.petcare.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.booking.entity.StaffUnavailableTime;
import com.petcare.booking.mapper.StaffUnavailableTimeMapper;
import com.petcare.booking.service.StaffUnavailableTimeService;
import org.springframework.stereotype.Service;

@Service
public class StaffUnavailableTimeServiceImpl extends ServiceImpl<StaffUnavailableTimeMapper, StaffUnavailableTime> implements StaffUnavailableTimeService {
}
