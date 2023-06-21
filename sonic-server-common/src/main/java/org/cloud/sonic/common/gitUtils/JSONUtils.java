package org.cloud.sonic.common.gitUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.cloud.sonic.common.gitUtils.exception.MsgException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @Author Mr.Lv
 * @Date 2020/8/31 11:10
 */
@Slf4j
public class JSONUtils {

//    public static Object toJson(String json) {
//        if (StringUtils.isEmpty(json)) {
//            return new JsonObject();
//        }
//
//        String subStr = json.substring(0, 1);
//        switch (subStr) {
//            case "[":
//                return JSONArray.parseArray(json);
//            case "{":
//                return JSONObject.parseObject(json);
//            default:
//                return new JsonObject();
//        }
//    }

    /**
     * js方式获取参数
     *
     * @param jsonObject
     * @param str
     * @return
     * @throws MsgException
     */
    public static Object jsGetData(Object jsonObject, String str) {
        if (!(jsonObject instanceof Map) && ObjectUtils.notIsEmpty(jsonObject)) {
            jsonObject = JSONUtils.toJSONObject(jsonObject);
        }

        String[] colNames = str.split("\\.");
        Object resultData = jsonObject;
        Integer num = 0;
        JSONArray jsonArray;
        for (int i = 0; i < colNames.length; i++) {
            if (ObjectUtils.valueVerification("isInteger", colNames[i])) {
                jsonArray = (JSONArray) resultData;
                try {
                    num = ObjectUtils.toInt(colNames[i]);
                } catch (MsgException e) {
                    num = 0;
                    log.warn("toInt()错误！！！");
                }
                if (null != jsonArray && num < jsonArray.size()) {
                    resultData = jsonArray.get(num);
                } else {
                    return null;
                }
            } else {
//                System.out.println(resultData.getClass());
//                System.out.println(JSO);
                if (resultData instanceof JSONArray) {
//                    log.info("resultData:{}", resultData);
                } else {
                    resultData = get((JSONObject) resultData, colNames[i]);
                }
            }
        }
        return resultData;
    }

    public static Object get(JSONObject jsonObject, String key) {
        if (ObjectUtils.isEmpty(jsonObject)) {
            return null;
        }
        Object value = jsonObject.get(key);
        if (value instanceof JSONObject) {
            return value;
        } else if (value instanceof Map) {
            return new JSONObject((Map) value);
        } else if (value instanceof JSONArray) {
            return value;
        } else if (value instanceof List) {
            return new JSONArray((List) value);
        }
        return value;
    }

    /**
     * 根据指定列（keyColName）分类 ?(valueColName)
     *
     * @param list
     * @param keyColName
     * @param valueColName
     * @return 分类
     */
    public static Map<Object, List<Object>> classify(List list, String keyColName, String valueColName) {
        JSONArray jsonArray = JSONUtils.toJSONArray(list);

        JSONObject jsonObject;
        Object key;
        Object value;

        Map<Object, List<Object>> obgM = new HashMap<>();
        List<Object> businessDevices;
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = jsonArray.getJSONObject(i);
            key = jsonObject.get(keyColName);
            value = jsonObject.get(valueColName);
            if (obgM.containsKey(key)) {
                businessDevices = obgM.get(key);
                businessDevices.add(value);
            } else {
                businessDevices = new ArrayList<>();
                businessDevices.add(value);
                obgM.put(key, businessDevices);
            }
        }
        return obgM;
    }

    public static Integer getInt(JSONObject jsonObject, String str) {
        String[] colNames = str.split("\\.");

        JSONObject resultData = null;
        for (int i = 0; i < colNames.length; i++) {
            if (i + 1 == colNames.length) {
                if (ObjectUtils.notIsEmpty(resultData)) {
                    return resultData.getInteger(colNames[i]);
                } else {
                    return jsonObject.getInteger(colNames[i]);
                }
            } else {
                resultData = jsonObject.getJSONObject(colNames[i]);
            }
        }
        return 0;
    }

    public static Double getDouble(JSONObject jsonObject, String str) {
        String[] colNames = str.split("\\.");

        JSONObject resultData = null;
        for (int i = 0; i < colNames.length; i++) {
            if (i + 1 == colNames.length) {
                if (ObjectUtils.notIsEmpty(resultData)) {
                    return resultData.getDouble(colNames[i]);
                } else {
                    return jsonObject.getDouble(colNames[i]);
                }
            } else {
                resultData = jsonObject.getJSONObject(colNames[i]);
            }
        }
        return 0.00;
    }

    public static JSONObject toJSONObject(Object data) {
        String json;
        if (data instanceof String) {
            json = (String) data;
//        } else if (data instanceof JSONObject) {
//            return (JSONObject) data;
        } else {
            json = JSONObject.toJSONString(data);
        }

        return JSONObject.parseObject(json);
    }

    public static JSONArray toJSONArray(Object data) {
        String json;
        if (data instanceof String) {
            json = (String) data;
        } else {
            json = JSONObject.toJSONString(data);
        }

        return JSONArray.parseArray(json);
    }

    /**
     * 废弃
     *
     * @param jsonObject
     * @param str
     * @return
     */
    public static Object getData(JSONObject jsonObject, String str) {
        String[] colNames = str.split("\\.");

        JSONObject resultData = null;
        for (int i = 0; i < colNames.length; i++) {
            if (i + 1 == colNames.length) {
                if (ObjectUtils.notIsEmpty(resultData)) {
                    return resultData.get(colNames[i]);
                } else {
                    return jsonObject.get(colNames[i]);
                }
            } else {
                resultData = jsonObject.getJSONObject(colNames[i]);
            }
        }
        return null;
    }

}
