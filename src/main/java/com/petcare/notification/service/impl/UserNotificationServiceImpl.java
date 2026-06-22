package com.petcare.notification.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.notification.entity.UserNotification;
import com.petcare.notification.mapper.UserNotificationMapper;
import com.petcare.notification.service.UserNotificationService;
import org.springframework.stereotype.Service;

@Service
public class UserNotificationServiceImpl extends ServiceImpl<UserNotificationMapper, UserNotification>
        implements UserNotificationService {
}
