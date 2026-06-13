package com.petcare.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.user.entity.UserSecurityQuestion;
import com.petcare.user.mapper.UserSecurityQuestionMapper;
import com.petcare.user.service.UserSecurityQuestionService;
import org.springframework.stereotype.Service;

@Service
public class UserSecurityQuestionServiceImpl
        extends ServiceImpl<UserSecurityQuestionMapper, UserSecurityQuestion>
        implements UserSecurityQuestionService {
}
