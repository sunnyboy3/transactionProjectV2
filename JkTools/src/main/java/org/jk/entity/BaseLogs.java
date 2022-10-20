package org.jk.entity;

/**
 * @ClassName BaseLogs
 * @Description 日志基础设置
 * @Author wp
 * @Date 2022/10/14 15:54
 **/
public class BaseLogs {
    private String transactionTraceId;

    public String getTransactionTraceId() {
        return transactionTraceId;
    }

    public void setTransactionTraceId(String transactionTraceId) {
        this.transactionTraceId = transactionTraceId;
    }
}
