package org.jk.core;

import brave.Tracing;
import brave.internal.baggage.BaggageFields;
import org.jk.utils.ApplicationContextUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @ClassName TraceIdManagerImpl
 * @Description 获取全局唯一ID
 * @Author wp
 * @Date 2022/10/13 17:17
 **/
public class TraceIdSleuthManagerImpl implements TraceIdManager{

    public static final String TRANSACTION_TRACE_ID = "transaction-trace-id";

    @Override
    public String getRequestHeaderTraceId(HttpServletRequest request) {
        return request.getHeader(TRANSACTION_TRACE_ID);
    }

    /**
     * 前端通过head设置路由通过该方法获取
     * @return
     */
    @Override
    public String getHeaderTraceId() {
        Tracing tracing = ApplicationContextUtils.getApplicationContext().getBean(Tracing.class);
        List<Object> extras = tracing.tracer().currentSpan().context().extra();
        String transactionTraceId = null;
        if (!CollectionUtils.isEmpty(extras)){
            for (Object o:extras) {
                if (o instanceof BaggageFields){
                    BaggageFields field = (BaggageFields)o;
                    if (!CollectionUtils.isEmpty(field.getAllValues().values())) {
                        transactionTraceId = field.getAllValues().values().stream().findFirst().get();
                    }
                }
            }
        }
        return transactionTraceId;
    }

    /**
     * 在Sleuth获取路由键
     * @return
     */
    @Override
    public String getTraceId() {
        Tracing tracing = ApplicationContextUtils.getApplicationContext().getBean(Tracing.class);
        return tracing.tracer().currentSpan().context().traceIdString();
    }

}
