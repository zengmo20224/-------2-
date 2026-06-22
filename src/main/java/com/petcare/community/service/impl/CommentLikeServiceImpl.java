package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.CommentLike;
import com.petcare.community.mapper.CommentLikeMapper;
import com.petcare.community.service.CommentLikeService;
import org.springframework.stereotype.Service;

@Service
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike>
        implements CommentLikeService {
}
