package com.mp.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mp.bean.User;

public interface UserMapper  extends BaseMapper<User> {

    User findById(Long id);
}
