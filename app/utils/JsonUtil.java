package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import java.util.HashMap;

/**
 * Created by User on 2017/8/17.
 */
public class JsonUtil {

    static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
        gson = gsonBuilder.create();
    }
    /**
     * 对序列化的Long类型进行特殊处理,避免位数过大导致和js精度的丢失,只用于向页面发送json数据时使用 js 最大 1<<57-1
     */
    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static void main(String[] args) {
        HashMap hashMap = new HashMap();
        hashMap.put("userId", 45203118504023803L);
        hashMap.put("datetime", 1500135055000L);
        String s = gson.toJson(hashMap);
        System.out.println(s);
    }
}
