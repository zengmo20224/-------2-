package com.petcare.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.admin.entity.AdminRolePermission;
import com.petcare.admin.mapper.AdminRolePermissionMapper;
import com.petcare.admin.service.AdminRolePermissionService;
import org.springframework.stereotype.Service;

@Service
public class AdminRolePermissionServiceImpl extends ServiceImpl<AdminRolePermissionMapper, AdminRolePermission> implements AdminRolePermissionService {
}
