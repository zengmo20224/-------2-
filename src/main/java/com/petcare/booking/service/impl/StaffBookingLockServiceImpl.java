package com.petcare.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.booking.entity.StaffBookingLock;
import com.petcare.booking.mapper.StaffBookingLockMapper;
import com.petcare.booking.service.StaffBookingLockService;
import org.springframework.stereotype.Service;

@Service
public class StaffBookingLockServiceImpl extends ServiceImpl<StaffBookingLockMapper, StaffBookingLock> implements StaffBookingLockService {
}
