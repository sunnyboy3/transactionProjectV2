package org.jk.interceptor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @ClassName BaseFilter
 * @Description 由于servletRequest只能够获取一次数据后面就无法获取到，在拦截器中需要将入参入库，造成后续逻辑代码无法获取到值
 * 这里复写ChildHttpServletRequestWrapper
 * @Author wp
 * @Date 2022/10/12 14:40
 **/
public class BaseFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        ChildHttpServletRequestWrapper requestWrapper = new ChildHttpServletRequestWrapper(httpServletRequest);
        filterChain.doFilter(requestWrapper, servletResponse);
    }
}
