package org.jk.entity;

import org.springframework.jdbc.core.JdbcTemplate;

public class GlobalTransaction {
    public static final String CLIENT_PREFIX = "org.jk.client.";
    public static final Integer SUCCESS_STATUS = 1;
    public JdbcTemplate jdbcTemplate;
    public static final String TRANSACTION_TRACE_ID = "transaction-trace-id";
    public static final String TRANSACTION_PACKAGE = "org.jk.controller";
    public static final String PARENT_NODE_ID = "parent-node";
}
