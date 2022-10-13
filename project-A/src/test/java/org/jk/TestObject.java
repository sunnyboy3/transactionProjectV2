package org.jk;

import org.jk.core.ResourceManager;
import org.jk.core.PostgreResourceManagerImpl;
import org.jk.utils.ApplicationContextUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
        ResourceManager resourceManager = (ResourceManager)ApplicationContextUtils.getApplicationContext().getBean(PostgreResourceManagerImpl.class);
//        resourceManager.saveLogs();
    }
}
