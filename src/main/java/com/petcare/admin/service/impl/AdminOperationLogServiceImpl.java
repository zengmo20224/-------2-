package com.petcare.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.admin.entity.AdminOperationLog;
import com.petcare.admin.mapper.AdminOperationLogMapper;
import com.petcare.admin.service.AdminOperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {

    /**
     * Saves a FAIL audit log in an independent transaction (REQUIRES_NEW).
     * Use this when the caller is about to throw — the FAIL log must survive
     * the rollback of the outer business transaction.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveFailLog(AdminOperationLog entity) {
        return super.save(entity);
    }
}
