package com.petcare.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.product.entity.ProductCarouselImage;
import com.petcare.product.mapper.ProductCarouselImageMapper;
import com.petcare.product.service.ProductCarouselImageService;
import org.springframework.stereotype.Service;

@Service
public class ProductCarouselImageServiceImpl extends ServiceImpl<ProductCarouselImageMapper, ProductCarouselImage>
        implements ProductCarouselImageService {
}
