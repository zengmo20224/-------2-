package com.petcare.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.booking.entity.ServiceBooking;
import com.petcare.booking.mapper.ServiceBookingMapper;
import com.petcare.booking.service.ServiceBookingService;
import org.springframework.stereotype.Service;

@Service
public class ServiceBookingServiceImpl extends ServiceImpl<ServiceBookingMapper, ServiceBooking> implements ServiceBookingService {
}
