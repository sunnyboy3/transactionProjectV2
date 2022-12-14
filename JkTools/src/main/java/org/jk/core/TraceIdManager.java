package org.jk.core;

import javax.servlet.http.HttpServletRequest;

public interface TraceIdManager {
    public String getTraceId();

    public String getHeaderTraceId();

    public String getRequestHeaderTraceId(HttpServletRequest request);

    public String getParentNode(HttpServletRequest request);

    public String getLocalSpanId();

    public String getParentSpanId();
}
