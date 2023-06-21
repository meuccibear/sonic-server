package org.cloud.sonic.common.gitUtils;

import com.alibaba.fastjson.TypeReference;

import java.util.List;

/**
 * @program: HNTD
 * @ClassName ArrUtils
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-03-10 17:47
 * @Version 1.0
 **/
public class ArrUtils {

    public static List<String> split(String s, String s1) {
        return BeanUtils.toJavaObject(s1.split(s), new TypeReference<List<String>>(){});
    }
}
