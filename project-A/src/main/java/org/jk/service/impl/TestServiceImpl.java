package org.jk.service.impl;

import org.jk.client.StockOpenFeignClient;
import org.jk.entity.ParentServiceImpl;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TestServiceImpl extends ParentServiceImpl implements TestService {

    @Resource
    private StockOpenFeignClient stockOpenFeignService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public User testMethod(User user) {

        if (1 == user.getFlag()){
            testWaitTimeOut();
        }
        User user1 = stockOpenFeignService.addStock(user);
        if (2 == user.getFlag()){
            testDatabases(user1);
        }
        if (3 == user.getFlag()){
            testSuccess(user1);
        }
        System.out.println("进入方法");
        System.out.println(user1.getUsername());
        user1.setUsername("zhangsan");
        return user1;
    }

    private void testSuccess(User user1){
        Object[] args = new Object[]{user1.getUsername(),user1.getAge()};
        String saveSql = "insert into service_public.transaction_user(username,age) values(?,?)";
        jdbcTemplate.update(saveSql,args);
    }

    /**
     * 模拟入库异常
     */
    private void testDatabases(User user1){
        Object[] args = new Object[]{user1.getUsername()};
        String saveSql = "insert into service_public.transaction_user(username) values(?)";
        jdbcTemplate.update(saveSql,args);
    }

    /**
     * 模拟请求超时异常
     */
    private void testWaitTimeOut(){
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
