package org.jk.aop;

import com.google.gson.Gson;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.ApplicationContextUtils;
import org.jk.utils.TransactionRequestLogsUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @ClassName TransactionAspect
 * @Description 截取参数值，以及返回参数值
 * @Author wp
 * @Date 2022/10/14 09:50
 **/
@Aspect
public class TransactionAspect {

    private final static int INIT_STATUS = 0;//初始状态

    @Pointcut("@annotation(org.jk.annotation.GlobalTransactional)")
    public void getParamResult(){}

    @Before("getParamResult()")
    public void inParamResult(JoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        GlobalTransactional globalTransactional = methodSignature.getMethod().getAnnotation(GlobalTransactional.class);
        String spELString = globalTransactional.name();

        //表示没有消息1、新进入请求方法  2、由于HttpAutoInterceptor拦截消息抛出异常日志没有写入
        TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
        if (Objects.isNull(logs)){
            String traceId = ApplicationContextUtils.getTraceIdManager().getTraceId();
            String transactionTraceId = logs.getTransactionTraceId();
            if (!StringUtils.isEmpty(transactionTraceId)){
                traceId = transactionTraceId;
            }
            //执行方法请求参数
            Object result = generateKeyBySpEL(spELString, joinPoint);
            TransactionRequestLogs logsOrg = new TransactionRequestLogs();
            logsOrg.setTrace_id(traceId);
            logsOrg.setProject_name(ApplicationContextUtils.getProjectName());
            if (Objects.nonNull(result)){
                logsOrg.setIn_param(new Gson().toJson(result));
            }
            logsOrg.setStatus(INIT_STATUS);
            logsOrg.setGroup_name(globalTransactional.groupName());
            logsOrg.setFeign_client_name(globalTransactional.feignClientName());
            logsOrg.setSort(globalTransactional.sort());
            ApplicationContextUtils.getResourceManager().saveLogs(logsOrg);
        }else {
            String transactionTraceId = logs.getTransactionTraceId();
            if (!StringUtils.isEmpty(transactionTraceId)) {
                //执行方法请求参数
                Object result = generateKeyBySpEL(spELString, joinPoint);
                logs.setProject_name(ApplicationContextUtils.getProjectName());
                if (Objects.nonNull(result)) {
                    logs.setIn_param(new Gson().toJson(result));
                }
                logs.setStatus(INIT_STATUS);
                logs.setGroup_name(globalTransactional.groupName());
                logs.setFeign_client_name(globalTransactional.feignClientName());
                logs.setSort(globalTransactional.sort());
                ApplicationContextUtils.getResourceManager().saveLogs(logs);
            }
        }
    }

    @AfterReturning(value = "getParamResult()",returning = "obj")
    private void outParamResult(JoinPoint joinPoint, Object obj){
        if (Objects.nonNull(obj)) {
            TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
            logs.setOut_param(new Gson().toJson(obj));
            ApplicationContextUtils.getResourceManager().updateLogsOutParam();
        }
    }

    private Object generateKeyBySpEL(String spELString,JoinPoint joinPoint) throws Throwable {
        if(!StringUtils.isEmpty(spELString)){
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();

            SpelExpressionParser parser = new SpelExpressionParser();
            Expression expression = parser.parseExpression(spELString);
            EvaluationContext context = new StandardEvaluationContext();

            Object[] args = joinPoint.getArgs();
            DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
            String[] parameterNames = discoverer.getParameterNames(method);

            if (parameterNames != null && parameterNames.length > 0 && args != null && args.length > 0){
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i],args[i]);
                }
                //解析,获取替换后的结果
                Object result = expression.getValue(context,Object.class);
                return result;
            }
        }
        return null;
    }

}
