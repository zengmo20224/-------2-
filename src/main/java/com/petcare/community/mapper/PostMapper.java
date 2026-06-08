package com.petcare.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.petcare.community.entity.Post;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
}
