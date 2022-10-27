package org.jk.utils;

import com.sun.javafx.fxml.builder.URLBuilder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName UrlParamUtils
 * @Description 格式化请求参数
 * @Author wp
 * @Date 2022/10/25 15:44
 **/
public class UrlParamUtils {
    public static Map<String,Object> asUrlParams(Map<String, String[]> source){
        Iterator<String> it = source.keySet().iterator();
//        StringBuilder paramStr = new StringBuilder();
        Map<String,Object> result = new LinkedHashMap<>();
        while (it.hasNext()){
            String key = it.next();
            String[] value = source.get(key);
            if (Objects.isNull(value) || value.length <= 0){
                continue;
            }
            String valueEncoder = "";
            try {
                valueEncoder = URLEncoder.encode(value[0], "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            result.put(key,valueEncoder);
//            paramStr.append("&").append(key).append("=").append(valueEncoder);
        }
        return result;
    }

}
