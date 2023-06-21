package org.cloud.sonic.common.gitUtils;

import com.alibaba.fastjson.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * List 转换类
 *
 * @Description:
 * @Date: Created in 2018/8/14 21:53
 * @Author: Wangll
 */
public class BeanUtils {

    protected final Log logger = LogFactory.getLog(BeanUtils.class);

    /**
     * 首字母转大写
     *
     * @param s 字符串
     * @return stirng
     * update wmk
     */
    private static String toUpcase(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
    }

    /**
     * 多个实体类的属性，合并到一个实体类
     * eg:
     * RiderUserBaseInfo riderUserBaseInfo = new RiderUserBaseInfo();
     * OrderStatistics orderStatistics = new OrderStatistics();
     * riderUserBaseInfo.setHeadImageUrl("asdasdasd");
     * orderStatistics.setOrderAmountTotal(1);
     * System.out.println(JSONObject.toJSONString(mergeObjects(RiderMyPageVO.class,riderUserBaseInfo,orderStatistics)));
     *
     * @param clazz
     * @param values 需要合并的实体类
     * @return Object
     */
    public static <T> T mergeObjects(Class<T> clazz, Object... values) {
        if (null == clazz) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        if (values != null) {
            for (Object o : values) {
                if (null != o) {
                    jsonObject.putAll(JSONUtils.toJSONObject(o));
                }
            }
        }
        String jsonStr = jsonObject.toJSONString();
        return JSONObject.parseObject(jsonStr, clazz);
    }


    public static <T> T mergeObjects(TypeReference<T> type, Object... values) {
        if (null == type) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        if (values != null) {
            for (Object o : values) {
                if (null != o) {
                    jsonObject.putAll(JSONUtils.toJSONObject(o));
                }
            }
        }
        String jsonStr = jsonObject.toJSONString();
        return toJavaObject(jsonStr, type);
    }

    public static <T> T mergeToModel(TypeReference<T> type, String parameter, Object data) {
        if (null == parameter) {
            return toJavaObject("{}", type);
        }
        JSONObject parameterJson = JSONUtils.toJSONObject(parameter);
        JSONObject dataJson = JSONUtils.toJSONObject(data);
        for (String s : parameterJson.keySet()) {
            parameterJson.put(s, dataJson.get(s));
        }
        return toJavaObject(parameterJson, type);
    }

    /**
     * 将一个实体类集合 换成 另一种实体类集合
     * eg:
     * List<AreaDistance> areaDistances = new ArrayList<>();
     * AreaDistance areaDistance = new AreaDistance();
     * areaDistance.setAmount(BigDecimal.valueOf(11));
     * areaDistances.add(areaDistance);
     * <p>
     * List<AreaDistanceVO> areaDistanceVOS = (List<AreaDistanceVO>) BeanUtils.exchangeObjectToList(AreaDistanceVO.class, areaDistances);
     *
     * @param clazz
     * @param list
     * @return
     */
    public static <T> List<T> exchangeObjectToList(Class<T> clazz, List<?> list) {
        List<T> objects = new ArrayList<T>();
        if (null != list) {
            for (Object object : list) {
                objects.add(mergeObjects(clazz, object));
            }
        }
        return objects;
    }

    /**
     * Object 转 任意实体类 或者 Map
     * eg:
     * LeanData result = BeanUtils.toJavaObject(xprinterResult.getResultV(), new TypeReference<LeanData>() {});
     *
     * @param obj  实体类
     * @param type 类型
     * @param <T>  任意实体类 或者 Map
     * @return
     */
    public static <T> T toJavaObject(Object obj, TypeReference<T> type) {
        String text = "";
        if(ObjectUtils.notIsEmpty(obj)){
            if (obj instanceof String) {
                text = (String) obj;
                if (ObjectUtils.isEmpty(text)) {
                    text = "{}";
                }

            } else {
                text = JSON.toJSONString(obj);
            }
            return JSONObject.parseObject(text, type);
        }
        return null;
    }

    public static Object toJSON(Object data) {
        String json;
        if (data instanceof String) {
            json = (String) data;
        } else {
            json = JSONObject.toJSONString(data);
        }

        try {
            return JSONObject.parseObject(json);
        } catch (JSONException e){
            System.out.println(e.toString());
        }
        return JSONArray.parseArray(json);
    }

}
