package org.jk.interceptor;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.GlobalTransaction;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 主要业务逻辑在拦截器中，后续会逐步重构代码
 */
public class HttpAutoInterceptor extends GlobalTransaction implements HandlerInterceptor, ApplicationContextAware, RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpAutoInterceptor.class);
    /**
     * 分布式补偿无法解决多线程访问的问题  需要在主线程中获取RequestAttributes  从新设置进
     * 不推荐多线程操作对端设置事务
     */
    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(attributes)) return;
        HttpServletRequest request = attributes.getRequest();
        String headerTraceID = request.getHeader(TRANSACTION_TRACE_ID);
        logger.info("设置请求消息头路由ID：{}",headerTraceID);
        if (!StringUtils.isEmpty(headerTraceID))template.header(TRANSACTION_TRACE_ID, headerTraceID);
        if (Objects.nonNull(TransactionRequestLogsUtils.getLogs())){
            logger.info("设置请求消息头该请求spanID：{}",TransactionRequestLogsUtils.getLogs().getLocal_node());
            template.header(PARENT_NODE_ID, TransactionRequestLogsUtils.getLogs().getLocal_node());
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
            String traceId = ApplicationContextUtils.getTraceIdManager().getTraceId();
            String transactionTraceId = ApplicationContextUtils.getTraceIdManager().getRequestHeaderTraceId(request);
            if (!StringUtils.isEmpty(transactionTraceId)){
                //表示补偿事务
                traceId = transactionTraceId;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            GlobalTransactional transactional = handlerMethod.getMethodAnnotation(GlobalTransactional.class);
            if (Objects.nonNull(transactional)) {
                List<TransactionRequestLogs> transactionRequestLogs = ApplicationContextUtils.getResourceManager().findLogsByTraceId(traceId);
                if (!CollectionUtils.isEmpty(transactionRequestLogs)) {
                    //表示非第一次请求
                    List<TransactionRequestLogs> selfRequestLogs = transactionRequestLogs.stream().sorted(Comparator.comparing(TransactionRequestLogs::getSort)).filter(k ->ApplicationContextUtils.getProjectName().equals(k.getProject_name()) && transactional.sort() == k.getSort()).collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(selfRequestLogs)) {
                        //已经存在
                        TransactionRequestLogs selfLogs = selfRequestLogs.get(0);
                        TransactionRequestLogsUtils.setLogs(selfLogs);
                        if (SUCCESS_STATUS.equals(selfLogs.getStatus())) {
                            List<TransactionRequestLogs> childRequestLogs = transactionRequestLogs.stream().filter(k ->Objects.nonNull(selfLogs.getLocal_node()) && selfLogs.getLocal_node().equals(k.getParent_node())).collect(Collectors.toList());
                            if (!CollectionUtils.isEmpty(childRequestLogs)){
                                List<TransactionRequestLogs> childSortLogs = childRequestLogs.stream().sorted(Comparator.comparing(TransactionRequestLogs::getSort)).collect(Collectors.toList());
                                for (TransactionRequestLogs logs:childSortLogs) {
                                    feignClientInvoke(logs,transactional);
                                }
                            }
                            if (!StringUtils.isEmpty(selfLogs.getOut_param()) && 1 == selfLogs.getStatus()){
                                //将输出参数输出以供后面调用
                                response.setStatus(200);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                PrintWriter writer = response.getWriter();
                                writer.write(selfLogs.getOut_param());
                                writer.flush();
                                writer.close();
                            }
                            return false;
                        }
                    } else {
                        if (!StringUtils.isEmpty(transactionTraceId)){
                            TransactionRequestLogs selfLogs = new TransactionRequestLogs();
                            selfLogs.setTransactionTraceId(transactionTraceId);
                            selfLogs.setParent_node(ApplicationContextUtils.getTraceIdManager().getParentNode(request));
                            TransactionRequestLogsUtils.setLogs(selfLogs);
                        }
                    }
                }
            }
        }
        return true;
    }

    private void feignClientInvoke(TransactionRequestLogs logs,GlobalTransactional transactional) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        //成功 需要将该工程的  addStock
        String[] clientMethod = logs.getFeign_client_name().split("#");
        Object bean = ApplicationContextUtils.getApplicationContext().getBean(CLIENT_PREFIX + clientMethod[0]);
        Method[] orgMethods = ClassLoader.getSystemClassLoader().loadClass(CLIENT_PREFIX + clientMethod[0]).getMethods();

        for (Method method: orgMethods) {
            if (clientMethod[1].equals(method.getName())) {

                Map<String,Object> resultMap = new LinkedHashMap<>();

                int parameterCount = method.getParameterCount();
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int i = 0; i < parameterCount; i++){
                    Annotation[] annotations = parameterAnnotations[i];
                    Method invokeMethod = getInvokeMethod(bean, clientMethod);
                    if (Objects.isNull(invokeMethod)){
                        return;
                    }
                    for (int j = 0; j < annotations.length; j++) {
                        if (annotations[j] instanceof RequestParam){
                            String value = ((RequestParam) annotations[j]).value();
                            String in_param = logs.getIn_param();
                            resultMap.put(value,invokeMethod.getParameterTypes()[i].cast(getValue(in_param, value)));
                            if ((i + 1) == parameterCount){
                                Object[] args = resultMap.values().stream().toArray();
                                invokeMethod.invoke(bean,args);
                                return;
                            }
                        } else if (annotations[j] instanceof RequestBody){
                            String in_param = logs.getIn_param();
                            if (StringUtils.isEmpty(in_param)) {
                                invokeMethod.invoke(bean);
                            } else {
                                //将类型反射出来
                                Class<?> aClass = ClassLoader.getSystemClassLoader().loadClass(invokeMethod.getParameterTypes()[i].getTypeName());
                                Object param = GsonAdapterUtils.getGson().fromJson(in_param,aClass);
                                invokeMethod.invoke(bean,param);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    private Object getValue(String in_param,String filed){
        Map<String, Object> resultMap = new HashMap<>();
        Map<String,Object> map = GsonAdapterUtils.getGson().fromJson(in_param, resultMap.getClass());
        return map.get(filed);
    }

    private Method getInvokeMethod(Object bean,String[] clientMethod){
        Method[] orgMethods = bean.getClass().getDeclaredMethods();
        if (Objects.isNull(orgMethods) || orgMethods.length <= 0){
            return null;
        }
        for (Method method : orgMethods) {
            if (clientMethod[1].equals(method.getName())) {
                return method;
            }
        }
        return null;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils utils = new ApplicationContextUtils();
        utils.setApplicationContext(applicationContext);
        //初始化事务数据
        operationDatabases();
    }

    private void operationDatabases(){
        //TODO 细节还需要优化如果动态tranceId表中存在数据则是不能进行删除操作
        ApplicationContextUtils.getResourceManager().deleteProjectData();
        ApplicationContextUtils.getResourceManager().saveProjectData();
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
