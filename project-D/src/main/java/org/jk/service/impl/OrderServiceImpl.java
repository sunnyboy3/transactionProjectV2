package org.jk.service.impl;

import org.jk.entity.Order;
import org.jk.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName OrderServiceImpl
 * @Description 订单服务
 * @Author wp
 * @Date 2022/10/18 10:20
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Transactional
    @Override
    public Order order(Order order) {
        System.out.println("进入逻辑区域");
        order.setOrderId("update data");
        if (3 == order.getFlag()){
            throw new RuntimeException("失败");
        }
        return order;
    }
}
