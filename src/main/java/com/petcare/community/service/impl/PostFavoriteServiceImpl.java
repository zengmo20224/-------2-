package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.PostFavorite;
import com.petcare.community.mapper.PostFavoriteMapper;
import com.petcare.community.service.PostFavoriteService;
import org.springframework.stereotype.Service;

@Service
public class PostFavoriteServiceImpl extends ServiceImpl<PostFavoriteMapper, PostFavorite> implements PostFavoriteService {
}
