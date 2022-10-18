package org.jk.controller;

import org.jk.annotation.GlobalTransactional;
import org.jk.entity.Order;
import org.jk.entity.User;
import org.jk.service.OrderService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @ClassName TestController
 * @Description 测试事务链路
 * @Author wp
 * @Date 2022/10/18 10:11
 **/
@RestController
@RequestMapping(value = "projectD")
public class TestController {

    @Resource
    private OrderService orderService;

    @GlobalTransactional(name = "#order",groupName = "gateway01#projectC01#projectD01",feignClientName = "OrderFeignClient#order",sort = 3)
    @RequestMapping(value = "order")
    public Order order(@RequestBody Order order){
        return orderService.order(order);
    }
}
