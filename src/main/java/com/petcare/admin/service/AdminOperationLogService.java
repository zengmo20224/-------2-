package com.petcare.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.admin.entity.AdminOperationLog;

public interface AdminOperationLogService extends IService<AdminOperationLog> {

    /**
     * Saves a FAIL audit log in an independent transaction so it survives
     * rollback of the caller's business transaction.
     */
    boolean saveFailLog(AdminOperationLog entity);
}
