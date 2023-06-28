package org.cloud.sonic.controller;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.cloud.sonic.common.gitUtils.JSONUtils;

@Slf4j
public class Demo {
    public static void main(String[] args) {
        JSONObject json = (JSONObject) JSONUtils.toJSON(System.getenv());
        for (Object o : json.keySet().toArray()) {
            log.info("{} | {}", o.toString(), json.get(o));
        }
    }
}
