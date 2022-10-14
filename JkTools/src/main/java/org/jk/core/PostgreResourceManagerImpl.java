package org.jk.core;

import com.google.gson.Gson;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionProject;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ClassName ResourceManager
 * @Description 日志资源管理
 * @Author wp
 * @Date 2022/10/13 14:27
 **/
public class PostgreResourceManagerImpl implements ResourceManager {

    private static final Logger logger = LoggerFactory.getLogger(PostgreResourceManagerImpl.class);

    private static final String SAVE_LOGS = "insert into service_public.transaction_request_logs(trace_id,project_name,in_param,status,group_name,feign_client_name,sort) values(?,?,?,?,?,?,?)";
    private static final String DELETE_PROJECT = "delete from service_public.transaction_project where project_name = ? and transaction_group = ? and feign_client_name = ?";
    private static final String SAVE_PROJECT = "insert into service_public.transaction_project(project_name,transaction_group,feign_client_name) values(?,?,?)";
    private static final String QUERY_LOGS_BY_TRACEID = "select id,trace_id,project_name,in_param,out_param,group_name,status,feign_client_name,sort from service_public.transaction_request_logs where trace_id = ?";
    private static final String UPDATE_LOGS_STATUS = "update service_public.transaction_request_logs set status = ? where trace_id = ? and project_name = ? and group_name = ? and feign_client_name = ?";
    private static final String UPDATE_LOGS_OUT_PARAM = "update service_public.transaction_request_logs set out_param = ? where trace_id = ? and project_name = ? and group_name = ? and feign_client_name = ?";
    public static final String TRANSACTION_PACKAGE = "org.jk.controller";


    private static final int INIT_LOGS_STATUS = 0;

    @Override
    public void updateLogsOutParam() {
        TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
        if (Objects.nonNull(logs)){
            JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);
            int num = jdbcTemplate.update(UPDATE_LOGS_OUT_PARAM,logs.getOut_param(),logs.getTrace_id(),logs.getProject_name(),logs.getGroup_name(),logs.getFeign_client_name());
            if (num == 0){
                logger.error("写入输入参数失败：Trace_id:{},Project_name:{},Group_name:{},Feign_client_name:{}",logs.getTrace_id(),logs.getProject_name(),logs.getGroup_name(),logs.getFeign_client_name());
            }
        }
    }

    @Override
    public void updateLogsStatus(TransactionRequestLogs logs) {

        JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);

        int num = jdbcTemplate.update(UPDATE_LOGS_STATUS,logs.getStatus(),logs.getTrace_id(),logs.getProject_name(),logs.getGroup_name(),logs.getFeign_client_name());
        if (num == 0){
            logger.error("transaction error! transaction desc: {}" + logs.toString());
        }
    }

    @Override
    public List<TransactionRequestLogs> findLogsByTraceId(String traceId) {

        JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);
        List<TransactionRequestLogs> transactionRequestLogs = jdbcTemplate.query(QUERY_LOGS_BY_TRACEID, new String[]{traceId}, new BeanPropertyRowMapper<TransactionRequestLogs>(TransactionRequestLogs.class) {
            @Override
            protected void initBeanWrapper(BeanWrapper bw) {
                super.initBeanWrapper(bw);
            }
        });
        return transactionRequestLogs;
    }

    @Override
    public void saveProjectData() {
        //获取注解数据
        operaProjectData(SAVE_PROJECT);
    }

    @Override
    public void deleteProjectData() {
        operaProjectData(DELETE_PROJECT);
    }

    private void operaProjectData(String operaSql){
        JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);
        //获取注解数据
        List<AnnotationEntity> values = AnnotationUtils.getRequestMappingValue(TRANSACTION_PACKAGE);
        if (!CollectionUtils.isEmpty(values)){
            List<TransactionProject> projects = values.stream().map(k -> {
                TransactionProject transactionProject = new TransactionProject();
                transactionProject.setProject_name(ApplicationContextUtils.getProjectName());
                transactionProject.setTransaction_group(k.getGroupName());
                transactionProject.setFeign_client_name(k.getFeignClientName());
                return transactionProject;
            }).collect(Collectors.toList());
            //与数据库比对
            List<Object[]> projectArray = projects.stream().map(k -> {
                return new String[]{k.getProject_name(), k.getTransaction_group(),k.getFeign_client_name()};
            }).collect(Collectors.toList());
            jdbcTemplate.batchUpdate(operaSql,projectArray);
        }
    }

    @Override
    public void saveLogs(GlobalTransactional transactional, Object inParam) {
        JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);
        try {
            Object[] args = new Object[]{ApplicationContextUtils.getTraceIdManager().getTraceId(),ApplicationContextUtils.getProjectName(),inParam,0,transactional.groupName(),transactional.feignClientName(),transactional.sort()};

            //存储日志信息
            TransactionRequestLogs logs = new TransactionRequestLogs();
            logs.setProject_name(ApplicationContextUtils.getProjectName());
            logs.setFeign_client_name(transactional.feignClientName());
            logs.setGroup_name(transactional.groupName());
            logs.setStatus(INIT_LOGS_STATUS);
            logs.setTrace_id(ApplicationContextUtils.getTraceIdManager().getTraceId());
            logs.setSort(transactional.sort());
            jdbcTemplate.update(SAVE_LOGS,args);
            TransactionRequestLogsUtils.setLogs(logs);
        }catch (Exception e){
            logger.error("新增数据失败");
        }
    }
}
