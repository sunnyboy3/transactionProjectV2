package org.jk.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

/**
 * @ClassName GsonAdapterUtils
 * @Description 包装gson属性类型
 * @Author wp
 * @Date 2022/10/27 09:47
 **/
public class GsonAdapterUtils {
    public static Gson getGson(){
        return new GsonBuilder().
                registerTypeAdapter(new TypeToken<Map<String,Object>>(){}.getType(),new ObjectTypeAdapterRewrite()).
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).
                setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    }
}
