
package org.jk.core;


import org.jk.annotation.GlobalTransactional;
import org.springframework.transaction.TransactionException;

import javax.servlet.http.HttpServletRequest;


public interface ResourceManager {

    public void saveLogs(GlobalTransactional transactional, HttpServletRequest request);

}
