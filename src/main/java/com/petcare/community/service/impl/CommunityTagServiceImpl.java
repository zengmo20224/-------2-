package com.petcare.community.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.community.entity.CommunityTag;
import com.petcare.community.mapper.CommunityTagMapper;
import com.petcare.community.service.CommunityTagService;
import org.springframework.stereotype.Service;

@Service
public class CommunityTagServiceImpl extends ServiceImpl<CommunityTagMapper, CommunityTag>
        implements CommunityTagService {
}
