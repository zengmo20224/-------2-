package com.petcare.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.admin.entity.AdminPermission;
import com.petcare.admin.mapper.AdminPermissionMapper;
import com.petcare.admin.service.AdminPermissionService;
import org.springframework.stereotype.Service;

@Service
public class AdminPermissionServiceImpl extends ServiceImpl<AdminPermissionMapper, AdminPermission> implements AdminPermissionService {
}
