package org.jk.aop;

import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.jk.annotation.GlobalParam;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.ApplicationContextUtils;
import org.jk.utils.GsonAdapterUtils;
import org.jk.utils.TransactionRequestLogsUtils;
import org.jk.utils.UrlParamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName TransactionAspect
 * @Description 截取参数值，以及返回参数值
 * @Author wp
 * @Date 2022/10/14 09:50
 **/
@Aspect
public class TransactionAspect {

    private static final Logger logger = LoggerFactory.getLogger(TransactionAspect.class);

    private final static int INIT_STATUS = 0;//初始状态

    @Pointcut("@annotation(org.jk.annotation.GlobalTransactional)")
    public void getParamResult(){}

    @Before("getParamResult()")
    public void inParamResult(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //如果没有注解则不进行拦截
        GlobalTransactional globalTransactional = methodSignature.getMethod().getAnnotation(GlobalTransactional.class);
        if (Objects.isNull(globalTransactional)){
            return;
        }
        GlobalParam[] params = globalTransactional.params();
        //表示没有消息1、新进入请求方法  2、由于HttpAutoInterceptor拦截消息抛出异常日志没有写入
        TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
        if (Objects.isNull(logs)){
            TransactionRequestLogs logsOrg = coverLogs(joinPoint,params,globalTransactional);
            String traceId = ApplicationContextUtils.getTraceIdManager().getTraceId();
            //执行方法请求参数
            logsOrg.setTrace_id(traceId);
            logsOrg.setParent_node(ApplicationContextUtils.getTraceIdManager().getParentSpanId());
            ApplicationContextUtils.getResourceManager().saveLogs(logsOrg);
        }else {
            String transactionTraceId = logs.getTransactionTraceId();
            if (!StringUtils.isEmpty(transactionTraceId)) {
                TransactionRequestLogs logsOrg = coverLogs(joinPoint,params,globalTransactional);
                logsOrg.setTrace_id(transactionTraceId);
                logsOrg.setParent_node(logs.getParent_node());
                ApplicationContextUtils.getResourceManager().saveLogs(logsOrg);
            }
        }
    }

    @AfterReturning(value = "getParamResult()",returning = "obj")
    private void outParamResult(JoinPoint joinPoint, Object obj){
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        logger.info("返回类型：{}",response.getContentType());
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //如果没有注解则不进行拦截
        GlobalTransactional globalTransactional = methodSignature.getMethod().getAnnotation(GlobalTransactional.class);
        if (Objects.isNull(globalTransactional)){
            return;
        }
        if (Objects.nonNull(obj)) {
            TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
            logs.setOut_param(GsonAdapterUtils.getGson().toJson(obj));
            ApplicationContextUtils.getResourceManager().updateLogsOutParam();
        }

    }


    private TransactionRequestLogs coverLogs(JoinPoint joinPoint,GlobalParam[] params,GlobalTransactional globalTransactional) throws Throwable {
        TransactionRequestLogs logsOrg = new TransactionRequestLogs();
        logsOrg.setProject_name(ApplicationContextUtils.getProjectName());
        logsOrg.setLocal_node(ApplicationContextUtils.getTraceIdManager().getLocalSpanId());
        logsOrg.setStatus(INIT_STATUS);
        logsOrg.setFeign_client_name(globalTransactional.feignClientName());
        logsOrg.setSort(globalTransactional.sort());
        logsOrg.setGroup_name(globalTransactional.groupName());
        setInParam(logsOrg,joinPoint,params);
        return logsOrg;
    }

    private void setInParam(TransactionRequestLogs requestLogs,JoinPoint joinPoint,GlobalParam[] params) throws Throwable{
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String contentType = request.getContentType();
        logger.info("请求类型：{}",contentType);
        Map<String,Object> result = generateKeyBySpEL(params, joinPoint);
        if (Objects.nonNull(result)){
            if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
                requestLogs.setIn_param(GsonAdapterUtils.getGson().toJson(result.values().stream().findFirst()));
            }else {
                requestLogs.setIn_param(GsonAdapterUtils.getGson().toJson(result));
            }
        }

    }

    private Map<String,Object> generateKeyBySpEL(GlobalParam[] params,JoinPoint joinPoint) throws Throwable {
        if(Objects.nonNull(params) && params.length > 0){
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            SpelExpressionParser parser = new SpelExpressionParser();
            List<Expression> expressions = Arrays.stream(params).map(k -> {
                Expression expression = parser.parseExpression("#" + k.name());
                return expression;
            }).collect(Collectors.toList());
            EvaluationContext context = new StandardEvaluationContext();
            Object[] args = joinPoint.getArgs();
            DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames = discoverer.getParameterNames(method);

            if (parameterNames != null && parameterNames.length > 0 && args != null && args.length > 0){
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i],args[i]);
                }
                //解析,获取替换后的结果
                Map<String,Object> result = new LinkedHashMap<>();
                expressions.stream().forEach(k ->{
                    result.put(k.getExpressionString().split("#")[1],k.getValue(context,Object.class));
                });
                return result;
            }
        }
        return null;
    }

}
