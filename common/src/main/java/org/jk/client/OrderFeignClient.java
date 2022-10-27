package org.jk.client;

import org.jk.entity.Order;
import org.jk.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @ClassName OrderFeignClient
 * @Description 订单接口
 * @Author wp
 * @Date 2022/10/18 10:23
 **/
@FeignClient(name = "project-D")
public interface OrderFeignClient {
    @RequestMapping(value = "/projectD/order",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    Order order(@RequestBody Object order);
}
