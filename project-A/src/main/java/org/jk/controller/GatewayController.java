package org.jk.controller;

import org.jk.annotation.GlobalParam;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "gateway")
public class GatewayController {
    @Resource
    private TestService testService;

    @GlobalTransactional(feignClientName = "GatewayControllerTestClient#test",sort = 0,params = {
            @GlobalParam(name ="user")
    })
    @RequestMapping(value = "test")
    public User test(@RequestBody(required = false) User user){
        return testService.testMethod(user);
    }

    @GlobalTransactional(feignClientName = "GatewayControllerTestClient#testParamMethod",sort = 0,params = {
            @GlobalParam(name = "username"),
            @GlobalParam(name = "password")
    })
    @GetMapping(value = "testParamMethod")
    public void testParamMethod(@RequestParam(value = "username") String username,@RequestParam(value = "password") String password){
        testService.test3(username,password);
    }
}
