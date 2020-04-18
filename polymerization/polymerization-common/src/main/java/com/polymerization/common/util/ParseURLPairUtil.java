package com.polymerization.common.util;

import com.polymerization.common.domain.BusinessException;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ParseURLPairUtil {

    public static String parseURLPair(Object o) throws BusinessException {
        Class<? extends Object> c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        Map<String, Object> map = new TreeMap<String, Object>();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = null;
            try {
                value = field.get(o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(value != null)
                map.put(name, value);
        }
        Set<Map.Entry<String, Object>> set = map.entrySet();
        Iterator<Map.Entry<String, Object>> it = set.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            Map.Entry<String, Object> e = it.next();
            sb.append(e.getKey()).append("=").append(e.getValue()).append("&");
        }
        return sb.deleteCharAt(sb.length()-1).toString();
    }
}
