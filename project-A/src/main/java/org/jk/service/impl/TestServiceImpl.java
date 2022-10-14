package org.jk.service.impl;

import org.jk.client.StockOpenFeignClient;
import org.jk.entity.ParentServiceImpl;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TestServiceImpl extends ParentServiceImpl implements TestService {

    @Resource
    private StockOpenFeignClient stockOpenFeignService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public User testMethod(User user) {
        User user1 = stockOpenFeignService.addStock(user);

        System.out.println("进入方法");
        System.out.println(user1.getUsername());
        user1.setUsername("zhangsan");
        return user1;
    }
}
