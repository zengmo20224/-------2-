package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.PostComment;
import com.petcare.community.mapper.PostCommentMapper;
import com.petcare.community.service.PostCommentService;
import org.springframework.stereotype.Service;

@Service
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {
}
