package org.jk.client;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "project-A")
public interface GatewayControllerTestClient {
    @RequestMapping("/gateway/test")
    @Headers({"Content-Type: application/json","Accept: application/json"})
    String test(@RequestBody Object obj);
    @RequestMapping("/gateway/testParamMethod")
    @Headers({"Content-Type: application/json","Accept: application/json"})
    String testParamMethod(@RequestBody Object obj);
}
