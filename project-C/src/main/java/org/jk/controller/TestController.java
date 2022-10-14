package org.jk.controller;

import org.jk.annotation.GlobalTransactional;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "projectC")
public class TestController {

    @Resource
    private TestService testService;

    @GlobalTransactional(name = "#user",groupName = "gateway01#projectC01",feignClientName = "StockOpenFeignClient#addStock",sort = 1)
    @RequestMapping(value = "test")
    public User test(@RequestBody User user){
        return testService.testMethod(user);
    }
}
