package org.jk.service;

import org.jk.entity.User;

import java.util.concurrent.ExecutionException;

public interface TestService {
    User testMethod(User user);

    void test3(String username, String password);

    void testCompletableFuture() throws ExecutionException, InterruptedException;
}
