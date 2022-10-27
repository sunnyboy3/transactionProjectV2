package org.jk.service.impl;

import org.jk.client.OrderFeignClient;
import org.jk.entity.Order;
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
    @Resource
    private OrderFeignClient orderFeignClient;

    @Transactional
    @Override
    public void test3(String username, String password,Double age) {
        System.out.println("GET请求方式");
        System.out.println("用户名："+username);
        System.out.println("密码："+password);
        System.out.println("年龄："+age);
    }

    @Transactional
    @Override
    public User testMethod(User user) {
        System.out.println(user.getUsername());
        System.out.println("===进入逻辑处理区===");
        user.setUsername("lisi");
        Order order = new Order();
        order.setOrderId("ceshi");
        order.setAmount(0.03d);
        order.setFlag(user.getFlag());
        Order order1 = orderFeignClient.order(order);
        user.setUsername(order1.getOrderId());
        if (2 == user.getFlag()){
            throw new RuntimeException("projectC抛出异常");
        }
        return user;
    }
}
