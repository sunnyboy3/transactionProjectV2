package org.jk.utils;

import brave.Tracing;
import brave.baggage.BaggageField;
import brave.internal.baggage.BaggageFields;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TraceIdUtils {
    public static String getTraceId(){
        Tracing tracing = ApplicationContextUtils.getApplicationContext().getBean(Tracing.class);
        return tracing.tracer().currentSpan().context().traceIdString();
    }
    public static String getTransactionTraceId(){
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

}
