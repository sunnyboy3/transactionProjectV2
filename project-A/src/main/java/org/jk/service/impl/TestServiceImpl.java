package org.jk.service.impl;

import com.google.common.collect.Lists;
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

        List<Integer> classIds = new ArrayList<Integer>();
        for (int i = 0; i < 10; i++) {
            classIds.add(i);
        }
        List<Integer> result = new ArrayList<>();
        classIds.forEach(k -> {
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                //这里采用feignClient的方式将每个线程中的请求路由到其他微服务中，进行计算获取到计算的结果，在该主线程中在做处理
                return stockOpenFeignService.testThreadMethod(k);
            }, asyncTaskExecutor).exceptionally(e -> {
                logger.error("当前线程{},异步执行失败", Thread.currentThread().getName(), e);
                throw new RuntimeException("失败");
            });
            try {
                result.add(future.get(5,TimeUnit.SECONDS));
            } catch (Exception e) {
                logger.error("当前线程{},异步执行失败", Thread.currentThread().getName(), e);
                throw new RuntimeException("失败");
            }
        });
        result.forEach(System.out::println);
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
