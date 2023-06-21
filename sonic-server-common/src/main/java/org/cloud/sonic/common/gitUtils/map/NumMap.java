package org.cloud.sonic.common.gitUtils.map;

import java.util.HashMap;

/**
 * @program: helium-pro-new
 * @ClassName NumMap
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-04-13 15:47
 * @Version 1.0
 **/
public class NumMap extends HashMap<String, Integer> {

    /**
     * 添加并且判断是否存在
     * @param key
     * @return 是否存在
     */
    public boolean add(String key) {
        if (containsKey(key)) {
            Integer a = get(key);
            put(key, a++);
            return true;
        }else{
            put(key, 1);
        }
        return false;
    }

}
