
package org.jk.core;


import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionProject;
import org.jk.entity.TransactionRequestLogs;
import org.springframework.transaction.TransactionException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


public interface ResourceManager {

    public void saveLogs(TransactionRequestLogs logsOrg);
    public void updateLogsOutParam();
    public void deleteProjectData();
    public void saveProjectData();
    public List<TransactionRequestLogs> findLogsByTraceId(String traceId);
    public void updateLogsStatus(TransactionRequestLogs logs);
    public List<TransactionProject> findLogsByGroup(String projectName, String groupName);
}
