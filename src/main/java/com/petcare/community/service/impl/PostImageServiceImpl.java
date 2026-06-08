package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.PostImage;
import com.petcare.community.mapper.PostImageMapper;
import com.petcare.community.service.PostImageService;
import org.springframework.stereotype.Service;

@Service
public class PostImageServiceImpl extends ServiceImpl<PostImageMapper, PostImage> implements PostImageService {
}
