package org.jk.service.impl;

import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TestServiceImpl implements TestService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public User testMethod(User user) {
        System.out.println(user.getUsername());
        System.out.println("===进入逻辑处理区===");
        user.setUsername("lisi");
        return user;
    }
}
