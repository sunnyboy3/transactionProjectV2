package org.jk.controller;

import org.jk.annotation.GlobalTransactional;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "gateway")
public class GatewayController {
    @Resource
    private TestService testService;

    @GlobalTransactional(groupName = "gateway01",feignClientName = "GatewayControllerTestClient#test",sort = 0)
    @RequestMapping(value = "test")
    public User test(@RequestBody(required = false) User user){
        return testService.testMethod(user);
    }
}
