package com.petcare.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.booking.entity.StaffSchedule;
import com.petcare.booking.mapper.StaffScheduleMapper;
import com.petcare.booking.service.StaffScheduleService;
import org.springframework.stereotype.Service;

@Service
public class StaffScheduleServiceImpl extends ServiceImpl<StaffScheduleMapper, StaffSchedule> implements StaffScheduleService {
}
