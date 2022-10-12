package org.jk.interceptor;

import com.google.gson.Gson;
import org.jk.annotation.GlobalTransactional;
import org.jk.entity.GlobalTransaction;
import org.jk.entity.TransactionRequestLogs;
import org.jk.utils.ApplicationContextUtils;
import org.jk.utils.TransactionRequestLogsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @ClassName ApiResponseBody
 * @Description 用于将出参更新到库中
 * @Author wp
 * @Date 2022/10/12 14:40
 **/
@RestControllerAdvice(basePackages = "org.jk.controller")
public class ApiResponseBody implements ResponseBodyAdvice<Object> {

    private static final Logger logger = LoggerFactory.getLogger(ApiResponseBody.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        Method method = methodParameter.getMethod();
        return method.isAnnotationPresent(GlobalTransactional.class);
    }

    @Override
    public Object beforeBodyWrite(Object msg, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        logger.info("controllerAdvice operaIng");
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest();
        if (MediaType.APPLICATION_JSON_VALUE.equals(servletRequest.getContentType())){
            TransactionRequestLogs logs = TransactionRequestLogsUtils.getLogs();
            if (Objects.nonNull(logs) && Objects.nonNull(msg)){
                String updateSql = "update service_public.transaction_request_logs set out_param = ? where trace_id = ? and project_name = ? and group_name = ? and feign_client_name = ?";
                Gson gson = new Gson();
                JdbcTemplate jdbcTemplate = ApplicationContextUtils.getApplicationContext().getBean(JdbcTemplate.class);
                int num = jdbcTemplate.update(updateSql,gson.toJson(msg),logs.getTrace_id(),logs.getProject_name(),logs.getGroup_name(),logs.getFeign_client_name());
                if (num == 0){
                    logger.error("写入输入参数失败：Trace_id:{},Project_name:{},Group_name:{},Feign_client_name:{}",logs.getTrace_id(),logs.getProject_name(),logs.getGroup_name(),logs.getFeign_client_name());
                }
            }
        }
        return msg;
    }
}
