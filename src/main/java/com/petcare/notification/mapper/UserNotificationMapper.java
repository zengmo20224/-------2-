package com.petcare.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.notification.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {
}
