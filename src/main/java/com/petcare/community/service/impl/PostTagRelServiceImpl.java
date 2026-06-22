package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.PostTagRel;
import com.petcare.community.mapper.PostTagRelMapper;
import com.petcare.community.service.PostTagRelService;
import org.springframework.stereotype.Service;

@Service
public class PostTagRelServiceImpl extends ServiceImpl<PostTagRelMapper, PostTagRel>
        implements PostTagRelService {
}
