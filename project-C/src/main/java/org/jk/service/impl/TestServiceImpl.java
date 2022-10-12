package org.jk.service.impl;

import org.jk.entity.User;
import org.jk.service.TestService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TestServiceImpl implements TestService {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Transactional
    @Override
    public User testMethod(User user) {
        if (4 == user.getFlag()){
            testWaitTimeOut();
        }
        System.out.println(user.getUsername());
        System.out.println("===进入逻辑处理区===");
        user.setUsername("lisi");
        if (5 == user.getFlag()){
            testDatabases(user);
        }

        if (6 == user.getFlag()){
            testSuccess(user);
        }
        return user;
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
