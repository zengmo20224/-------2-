package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.product.entity.ProductOrder;
import com.petcare.product.mapper.ProductOrderMapper;
import com.petcare.product.service.ProductOrderService;
import org.springframework.stereotype.Service;

@Service
public class ProductOrderServiceImpl extends ServiceImpl<ProductOrderMapper, ProductOrder> implements ProductOrderService {
}
