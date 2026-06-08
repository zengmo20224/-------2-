package com.petcare.staff.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.staff.entity.StaffSkill;
import com.petcare.staff.mapper.StaffSkillMapper;
import com.petcare.staff.service.StaffSkillService;
import org.springframework.stereotype.Service;

@Service
public class StaffSkillServiceImpl extends ServiceImpl<StaffSkillMapper, StaffSkill> implements StaffSkillService {
}
