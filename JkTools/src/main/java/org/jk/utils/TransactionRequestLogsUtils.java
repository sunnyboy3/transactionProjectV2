package org.jk.utils;

import org.jk.entity.TransactionRequestLogs;
import org.springframework.context.ApplicationContext;

public class TransactionRequestLogsUtils {
    static ThreadLocal<TransactionRequestLogs> threadLocal = new ThreadLocal<>();

    public TransactionRequestLogsUtils(){}

    public static void setLogs(TransactionRequestLogs logs) {
        threadLocal.set(logs);
    }
    public static TransactionRequestLogs getLogs(){
        return threadLocal.get();
    }
    public static void remove(){
        threadLocal.remove();
    }
}
