package org.jk.service;

import org.jk.entity.User;

public interface TestService {
    User testMethod(User user);

    void test3(String username, String password,Double age);
}
