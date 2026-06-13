package com.petcare.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.common.exception.BusinessException;
import com.petcare.common.exception.ErrorCode;
import com.petcare.user.entity.User;
import com.petcare.user.mapper.UserMapper;
import com.petcare.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User lockActiveUser(Long userId) {
        User user = getBaseMapper().selectActiveForUpdate(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "用户不存在或已禁用");
        }
        return user;
    }
}
