package org.jk.core;

import javax.servlet.http.HttpServletRequest;

public interface TraceIdManager {
    public String getTraceId();

    public String getHeaderTraceId();

    public String getRequestHeaderTraceId(HttpServletRequest request);
}
