package org.jk.utils;

import com.google.gson.Gson;
import org.jk.interceptor.ChildHttpServletRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;

public class RequestUtils {
    public synchronized static String request2JsonString(ChildHttpServletRequestWrapper requestWrapper) throws IOException {
        InputStreamReader isr = new InputStreamReader(requestWrapper.getInputStream(),"utf-8");
        String result = "";
        int respCount = isr.read();
        while (respCount != -1){
            result += (char)respCount;
            respCount = isr.read();
        }
        return result;
    }
}
