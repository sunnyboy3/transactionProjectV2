package org.jk.service.impl;

import org.jk.client.StockOpenFeignClient;
import org.jk.entity.ParentServiceImpl;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

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
        if (1 == user.getFlag()){
            throw new RuntimeException("projectA抛出异常");
        }
        return Optional.ofNullable(stockOpenFeignService.test2(user1)).orElseThrow(() -> new RuntimeException("失败"));
    }
    @Transactional
    @Override
    public void test3(String username, String password) {
        stockOpenFeignService.test3(username,password,10.3d);
    }
}
