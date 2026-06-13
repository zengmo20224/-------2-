package com.petcare.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.user.entity.User;

public interface UserService extends IService<User> {

    /**
     * Locks the ACTIVE user row with SELECT FOR UPDATE.
     * Must be called within a transaction to serialize concurrent default address changes.
     */
    User lockActiveUser(Long userId);
}
