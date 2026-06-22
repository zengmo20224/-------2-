package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.product.entity.ProductDetailImage;
import com.petcare.product.mapper.ProductDetailImageMapper;
import com.petcare.product.service.ProductDetailImageService;
import org.springframework.stereotype.Service;

@Service
public class ProductDetailImageServiceImpl
        extends ServiceImpl<ProductDetailImageMapper, ProductDetailImage>
        implements ProductDetailImageService {
}
