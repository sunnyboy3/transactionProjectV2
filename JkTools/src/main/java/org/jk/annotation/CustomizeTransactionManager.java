package org.jk.annotation;

import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.ApplicationContextUtils;
import org.jk.utils.TransactionRequestLogsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.util.Objects;

/**
 * 事务处理设置日志状态
 */
public class CustomizeTransactionManager extends DataSourceTransactionManager {

    private final Logger logger = LoggerFactory.getLogger(CustomizeTransactionManager.class);

    private static final Integer SUCCESS_STATUS = 1;
    private static final Integer FAIL_STATUS = 2;

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
        if (Objects.nonNull(logs)) {
            logs.setStatus(SUCCESS_STATUS);
            ApplicationContextUtils.getResourceManager().updateLogsStatus(logs);
        }
        super.doCommit(status);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        logger.error("doRollback...");
        super.doRollback(status);//注意这里有一个坑，回滚要放到最前面，等回滚完了，在更新日志状态，不然更新日志状态会一起回滚
        TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
        if (Objects.nonNull(logs)){
            logs.setStatus(FAIL_STATUS);
            ApplicationContextUtils.getResourceManager().updateLogsStatus(logs);
        }

    }
}
