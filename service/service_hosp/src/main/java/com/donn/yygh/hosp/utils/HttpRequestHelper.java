package com.donn.yygh.hosp.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author Donn
 * @Date 2022/9/26 11:33
 **/
public class HttpRequestHelper {
    public static Map<String, Object> switchMap(Map<String, String[]> parameterMap) {
        Map<String,Object> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            map.put(entry.getKey(),entry.getValue()[0]);
        }
        return map;
    }
}
