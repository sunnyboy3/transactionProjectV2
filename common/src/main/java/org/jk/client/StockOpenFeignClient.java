package org.jk.client;

import feign.Headers;
import org.jk.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "project-C")
public interface StockOpenFeignClient{

    @RequestMapping(value = "/projectC/test",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_VALUE)
    User addStock(@RequestBody Object obj);
}
