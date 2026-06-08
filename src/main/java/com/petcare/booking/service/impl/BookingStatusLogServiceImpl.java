package com.petcare.booking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.booking.entity.BookingStatusLog;
import com.petcare.booking.mapper.BookingStatusLogMapper;
import com.petcare.booking.service.BookingStatusLogService;
import org.springframework.stereotype.Service;

@Service
public class BookingStatusLogServiceImpl extends ServiceImpl<BookingStatusLogMapper, BookingStatusLog> implements BookingStatusLogService {
}
