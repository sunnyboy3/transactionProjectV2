package org.jk;

import org.jk.client.StockOpenFeignClient;
import org.jk.utils.ApplicationContextUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.lang.reflect.Method;

/**
 * @ClassName TestObject
 * @Description TODO
 * @Author wp
 * @Date 2022/10/13 14:53
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestObject {

    @Test
    public void getResourceManager(){
        StockOpenFeignClient stockOpenFeignClient = ApplicationContextUtils.getApplicationContext().getBean(StockOpenFeignClient.class);
        Method[] methods = stockOpenFeignClient.getClass().getMethods();
        for (Method method: methods) {
            if ("test3".equals(method.getName())){


            }
        }
        System.out.println(methods);
//        resourceManager.saveLogs();
    }
}
