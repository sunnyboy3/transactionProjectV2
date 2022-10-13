package org.jk.core;

import org.jk.annotation.CustomizeTransactionManager;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionRequestLogs;
import org.jk.interceptor.ChildHttpServletRequestWrapper;
import org.jk.interceptor.HttpAutoInterceptor;
import org.jk.utils.ApplicationContextUtils;
import org.jk.utils.RequestUtils;
import org.jk.utils.TraceIdUtils;
import org.jk.utils.TransactionRequestLogsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @ClassName ResourceManager
 * @Description TODO
 * @Author wp
 * @Date 2022/10/13 14:27
 **/
public class ResourceManagerImpl implements ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceManagerImpl.class);

    private static final String SAVE_LOGS = "insert into service_public.transaction_request_logs(trace_id,project_name,in_param,status,group_name,feign_client_name,sort) values(?,?,?,?,?,?,?)";

    private static final int INIT_LOGS_STATUS = 0;

    @Resource
    private CustomizeTransactionManager customizeTransactionManager;

    @Override
    public void saveLogs(GlobalTransactional transactional, HttpServletRequest request) {
        DataSource dataSource = customizeTransactionManager.getDataSource();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement saveLogs = null;
            try {
                saveLogs = conn.prepareStatement(SAVE_LOGS);
                saveLogs.setString(1, TraceIdUtils.getTraceId());
                saveLogs.setString(2, ApplicationContextUtils.getProjectName());
                saveLogs.setString(3, coverRequestParam(request));
                saveLogs.setInt(4, INIT_LOGS_STATUS);
                saveLogs.setString(5,transactional.groupName());
                saveLogs.setString(6,transactional.feignClientName());
                saveLogs.setInt(7,transactional.sort());
                int saveRows = 0;
                try {
                    saveRows = saveLogs.executeUpdate();
                    if (saveRows > 0 && !conn.getAutoCommit()) {
                        conn.commit();
                    }
                }catch (SQLException e){
                    if (saveRows > 0 && !conn.getAutoCommit()) {
                        conn.rollback();
                    }
                    throw e;
                }

            } catch (Exception e) {
                if (!(e instanceof SQLException)) {
                    e = new SQLException(e);
                }
                throw (SQLException) e;
            } finally {
                if (saveLogs != null) {
                    saveLogs.close();
                }
            }
        } catch (Exception e) {
            logger.error("写入日志失败");
        }finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException closeEx) {
                    logger.warn("关闭数据库连接异常 ");
                }
            }
        }
    }

    private String coverRequestParam(HttpServletRequest request) throws IOException {
        ChildHttpServletRequestWrapper requestWrapper = new ChildHttpServletRequestWrapper(request);
        String contentType = requestWrapper.getContentType();
        if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)){
            return RequestUtils.request2JsonString(requestWrapper);
        }
        return "";
    }
}
