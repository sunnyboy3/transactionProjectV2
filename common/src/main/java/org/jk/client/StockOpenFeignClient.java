package org.jk.client;

import org.jk.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "project-C")
public interface StockOpenFeignClient{

    @RequestMapping(value = "/projectC/test",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    User addStock(@RequestBody Object obj);

    @RequestMapping(value = "/projectC/test2",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    User test2(@RequestBody Object obj);

    @RequestMapping(value = "/projectC/test3",method = RequestMethod.GET)
    void test3(@RequestParam(value = "username")String username,@RequestParam(value = "password")String password,@RequestParam(value = "age") Double age);
    @GetMapping("/projectC/testThreads")
    public Integer testThreadMethod(@RequestParam(value = "num") Integer num);
}
