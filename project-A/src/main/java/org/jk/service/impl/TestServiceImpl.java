package org.jk.service.impl;

import org.jk.client.StockOpenFeignClient;
import org.jk.entity.ParentServiceImpl;
import org.jk.entity.User;
import org.jk.service.TestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class TestServiceImpl extends ParentServiceImpl implements TestService {

    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);

    @Resource
    private StockOpenFeignClient stockOpenFeignService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    @Qualifier("asyncTaskExecutor")
    private ThreadPoolTaskExecutor asyncTaskExecutor;

    /**
     * 经过测试如果对端返回失败，或者客户端调用超时都会返回 捕获异常时的返回值
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void testCompletableFuture() throws ExecutionException, InterruptedException {

        List<Integer> nameList = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            nameList.add(i);
        }
        //从主线程获取所有request数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        List<String> list = nameList.stream().map(k -> {
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                logger.info("传递进入的数据:{}", k);
                //其他线程放入request数据
                RequestContextHolder.setRequestAttributes(requestAttributes);
                return "success"+stockOpenFeignService.testThreadMethod(k);
            }, asyncTaskExecutor).exceptionally(e -> {
                logger.error("当前线程{},异步执行失败", Thread.currentThread().getName(), e);
                return "fail"+k;
            });
            try {
                return future.get(20,TimeUnit.SECONDS);
            } catch (Exception e) {
                e.printStackTrace();
                return "fail"+k;
            }

        }).collect(Collectors.toList());
        list.forEach(System.out::println);
    }

    @Transactional
    @Override
    public User testMethod(User user) {
        User user1 = stockOpenFeignService.addStock(user);
        System.out.println("进入方法");
        System.out.println(user1.getUsername());
        if (1 == user.getFlag()){
            throw new RuntimeException("projectA抛出异常");
        }
        return Optional.ofNullable(stockOpenFeignService.test2(user1)).orElseThrow(() -> new RuntimeException("失败"));
    }
    @Transactional
    @Override
    public void test3(String username, String password) {
        stockOpenFeignService.test3(username,password,10.3d);
    }
}
