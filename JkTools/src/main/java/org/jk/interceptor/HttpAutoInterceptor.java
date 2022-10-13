package org.jk.interceptor;

import com.google.gson.Gson;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.jk.annotation.GlobalTransactional;
import org.jk.core.ResourceManager;
import org.jk.core.ResourceManagerImpl;
import org.jk.entity.GlobalTransaction;
import org.jk.entity.TransactionProject;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 主要业务逻辑在拦截器中，后续会逐步重构代码
 */
public class HttpAutoInterceptor extends GlobalTransaction implements HandlerInterceptor, ApplicationContextAware, RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpAutoInterceptor.class);

    @Override
    public void apply(RequestTemplate template) {
        if (!StringUtils.isEmpty(TraceIdUtils.getTransactionTraceId())){
            template.header(TRANSACTION_TRACE_ID, TraceIdUtils.getTransactionTraceId());
        }
    }

    /**
     * 逻辑执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //TODO 这里需要验证 只有注释了全局事务的才走里面
        return checkAllSuccess(request,response,handler);
    }

    private boolean checkAllSuccess(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            String traceId = TraceIdUtils.getTraceId();
            String transactionTraceId = request.getHeader(TRANSACTION_TRACE_ID);
            if (!StringUtils.isEmpty(transactionTraceId)){
                traceId = transactionTraceId;
            }
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            GlobalTransactional transactional = handlerMethod.getMethodAnnotation(GlobalTransactional.class);
            if (Objects.nonNull(transactional)) {
                List<TransactionRequestLogs> transactionRequestLogs = super.jdbcTemplate.query("select id,trace_id,project_name,in_param,out_param,group_name,status,feign_client_name,sort from service_public.transaction_request_logs where trace_id = ?", new String[]{traceId}, new BeanPropertyRowMapper<TransactionRequestLogs>(TransactionRequestLogs.class) {
                    @Override
                    protected void initBeanWrapper(BeanWrapper bw) {
                        super.initBeanWrapper(bw);
                    }
                });
                if (!CollectionUtils.isEmpty(transactionRequestLogs)) {
                    //表示非第一次请求
                    List<TransactionRequestLogs> selfRequestLogs = transactionRequestLogs.stream().sorted(Comparator.comparing(TransactionRequestLogs::getSort)).filter(k ->ApplicationContextUtils.getProjectName().equals(k.getProject_name()) && transactional.groupName().equals(k.getGroup_name()) && transactional.sort() == k.getSort()).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(selfRequestLogs)) {
                        //已经存在
                        TransactionRequestLogs selfLogs = selfRequestLogs.get(0);
                        TransactionRequestLogsUtils.setLogs(selfLogs);
                        if (SUCCESS_STATUS.equals(selfLogs.getStatus())) {
                            List<TransactionRequestLogs> childRequestLogs = transactionRequestLogs.stream().filter(k -> isNextNode(selfLogs.getGroup_name(),k.getGroup_name())).collect(Collectors.toList());
                            if (!CollectionUtils.isEmpty(childRequestLogs)){
                                List<TransactionRequestLogs> childSortLogs = childRequestLogs.stream().sorted(Comparator.comparing(TransactionRequestLogs::getSort)).collect(Collectors.toList());
                                for (TransactionRequestLogs logs:childSortLogs) {
                                    feignClientInvoke(logs);
                                }
                            }
                            if (!StringUtils.isEmpty(selfLogs.getOut_param()) && 1 == selfLogs.getStatus()){
                                //将输出参数输出以供后面调用
                                response.setStatus(200);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.getWriter().write(selfLogs.getOut_param());
                            }
                            return false;
                        }
                    } else {
                        //不存在数据
                        saveProjectData(transactional, request);
                    }
                } else {
                    saveProjectData(transactional, request);
                }
            }
        }
        return true;
    }

    private boolean isNextNode(String localGroup,String group){
        if (localGroup.equals(group)){
            return false;
        }
        if (localGroup.length() > group.length()){
            return false;
        }
        localGroup = localGroup + "#";
        String[] arr = group.split(localGroup);
        if (arr.length <=0){
            logger.error("规则不匹配请检查：本工程组名->{},匹配工程组名->{}",localGroup,group);
        }
        return !Arrays.stream(arr).filter(k -> !StringUtils.isEmpty(k)).findFirst().get().contains("#");
    }

    private void feignClientInvoke(TransactionRequestLogs logs) throws InvocationTargetException, IllegalAccessException {
        //成功 需要将该工程的  addStock
        String[] clientMethod = logs.getFeign_client_name().split("#");
        Object bean = ApplicationContextUtils.getApplicationContext().getBean(CLIENT_PREFIX + clientMethod[0]);
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method :
                methods) {
            if (clientMethod[1].equals(method.getName())) {
                String in_param = logs.getIn_param();
                if (StringUtils.isEmpty(in_param)) {
                    method.invoke(bean);
                }else {
                    Gson gson = new Gson();
                    method.invoke(bean,gson.fromJson(in_param,Object.class));
                }
            }
        }
    }

    private void saveProjectData(GlobalTransactional transactional,HttpServletRequest request) throws IOException {
        ResourceManager resourceManager = (ResourceManager)ApplicationContextUtils.getApplicationContext().getBean(ResourceManagerImpl.class);
        resourceManager.saveLogs(transactional,request);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils utils = new ApplicationContextUtils();
        utils.setApplicationContext(applicationContext);
        //初始化事务数据
        operationDatabases();
    }

    private void operationDatabases(){
        //这里初始化事务组数据
        String projectName = ApplicationContextUtils.getProjectName();
        //获取jdbcTemplate
        super.jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);

        //获取注解数据
        List<AnnotationEntity> values = AnnotationUtils.getRequestMappingValue(TRANSACTION_PACKAGE);
        if (!CollectionUtils.isEmpty(values)){
            List<TransactionProject> projects = values.stream().map(k -> {
                TransactionProject transactionProject = new TransactionProject();
                transactionProject.setProject_name(projectName);
                transactionProject.setTransaction_group(k.getGroupName());
                transactionProject.setFeign_client_name(k.getFeignClientName());
                return transactionProject;
            }).collect(Collectors.toList());
            //与数据库比对 TODO 细节还需要优化如果动态tranceId表中存在数据则是不能进行删除操作
            String deleteSql = "delete from service_public.transaction_project where project_name = ? and transaction_group = ? and feign_client_name = ?";
            List<Object[]> projectArray = projects.stream().map(k -> {
                return new String[]{k.getProject_name(), k.getTransaction_group(),k.getFeign_client_name()};
            }).collect(Collectors.toList());
            super.jdbcTemplate.batchUpdate(deleteSql,projectArray);
            String saveSql = "insert into service_public.transaction_project(project_name,transaction_group,feign_client_name) values(?,?,?)";
            super.jdbcTemplate.batchUpdate(saveSql,projectArray);
        }
    }

    /**
     * 逻辑执行完毕，视图解析器还未进行解析之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {

    }

    /**
     * 逻辑和视图执行器执行完毕
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TransactionRequestLogsUtils.remove();
    }
}
