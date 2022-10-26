package org.jk.controller;

import org.jk.annotation.GlobalTransactional;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping(value = "projectC")
public class TestController {

    @Resource
    private TestService testService;

    @GlobalTransactional(name = "#user",feignClientName = "StockOpenFeignClient#addStock",sort = 1)
    @RequestMapping(value = "test")
    public User test(@RequestBody User user){
        return testService.testMethod(user);
    }

    /**
     * name 入参对象名称
     * groupName 组名称 被调用 gateway01#projectC02 必须与gateway01组处在同一组里
     * feignClientName 请求客户端与方法组合
     * @param user
     * @return
     */
    @GlobalTransactional(name = "#user",feignClientName = "StockOpenFeignClient#test2",sort = 2)
    @RequestMapping(value = "test2")
    public User test2(@RequestBody User user){
        return testService.testMethod(user);
    }
    @GlobalTransactional(name = "#username",feignClientName = "StockOpenFeignClient#test3",sort = 2)
    @GetMapping(value = "test3")
    public void test3(@RequestParam(value = "username") String username,@RequestParam(value = "password") String password){
        testService.test3(username,password);
    }
}
