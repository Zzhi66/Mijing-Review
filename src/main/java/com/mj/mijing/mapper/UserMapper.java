package com.mj.mijing.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mj.mijing.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
