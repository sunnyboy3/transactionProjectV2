
package org.jk.core;


import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionRequestLogs;
import org.springframework.transaction.TransactionException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface ResourceManager {

    public void saveLogs(GlobalTransactional transactional, Object inParam);
    public void updateLogsOutParam();
    public void deleteProjectData();
    public void saveProjectData();
    public List<TransactionRequestLogs> findLogsByTraceId(String traceId);
    public void updateLogsStatus(TransactionRequestLogs logs);
}
