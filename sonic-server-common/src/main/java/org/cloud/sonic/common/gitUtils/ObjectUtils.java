package org.cloud.sonic.common.gitUtils;

import com.alibaba.fastjson.JSONObject;
import org.cloud.sonic.common.gitUtils.exception.MsgException;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: DemoJava
 * @description:
 * @author: Zhuozhuang.Lv
 * @create: 2020-01-06 16:20
 */

public class ObjectUtils {

    public static Boolean isEmpty(Object value) {
        return !notIsEmpty(value);
    }

    public static Boolean notIsEmpty(Object value) {
        if (value == null) {
            return false;
        } else if (value instanceof String) {
            return ((String) value).length() > 0;
        } else if (value instanceof String[]) {
            return ((String[]) value).length > 0;
        } else if (value instanceof Object[]) {
            return ((Object[]) value).length > 0;
//        } else if (value instanceof Long) {
//            return (Long) value > 0;
//        } else if (value instanceof Integer) {
//            return (Integer) value > 0;
        } else if (value instanceof List) {
            List list = (List) value;
            return list.size() > 0;
        } else if (value instanceof Map) {
            Map map = (Map) value;
            return map.size() > 0;
        }
        return true;
    }


    public static void main(String[] args) {
        System.out.println(valueVerification("isJSONArray", "[]"));
    }

    /**
     * 去除空格
     *
     * @param str 字符串
     * @return 去除空格后的字符串
     */
    public static String clearSpace(String str) {
        return str.replaceAll(" ", "");
    }

    /**
     * 字符串 区间截取 【去除空格】
     *
     * @param str      字符串
     * @param beginStr 开始 字符串
     * @param endStr   结束字符串
     * @return 返回截取的字符串
     */
    public static String substring(String str, String beginStr, String endStr) {
        int beginIndex = str.indexOf(beginStr);
        if (beginIndex > -1) {
            String lastStr = str.substring(beginIndex + beginStr.length(), str.length());
            int lastIndex = lastStr.indexOf(endStr);
            if (lastIndex > -1) {
                return lastStr.substring(0, lastIndex).toString().replaceAll(" ", "");
            } else {
                return "";
            }
        }
        return "";
    }


    /**
     * 字符串 区间截取 【去除空格】
     *
     * @param str      字符串
     * @param beginStr 开始 字符串
     * @return 返回截取的字符串
     */
    public static String substring(String str, String beginStr) {
        int beginIndex = str.indexOf(beginStr);
        if (beginIndex > -1) {
            String lastStr = str.substring(beginIndex + beginStr.length(), str.length());
            int lastIndex = lastStr.length();
            if (lastIndex > -1) {
                return lastStr.substring(0, lastIndex).toString().replaceAll(" ", "");
            } else {
                return "";
            }
        }
        return "";
    }

    public static String getString(String res, String regex) {
        // 定义一个样式模板，此中使用正则表达式，括号中是要抓的内容
        // 相当于埋好了陷阱匹配的地方就会掉下去
        Pattern pattern = Pattern.compile(regex);
        // 定义一个matcher用来做匹配
        Matcher matcher = pattern.matcher(res);
        // 如果找到了
        if (matcher.find()) {
            // 打印出结果
            return matcher.group(1);
        }

        return "";
    }

    /**
     * 转 utl
     *
     * @param obj 对象
     * @return url字符串
     */
    public static String toUrl(Object obj) {
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(obj);
        Iterator<String> iterator = jsonObject.keySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = (String) jsonObject.get(key);
            if (ObjectUtils.notIsEmpty(value)) {
                stringBuilder.append(String.format("%s=%s", key, value)).append("; ");
            }
        }
        return stringBuilder.toString();
    }


    /**
     * 中文时间单位转数字
     *
     * @param str 中文时间单位
     * @return 数字
     */
    public static Long replaceTimeChinese(String str) {
        Long returnLong = 0L;

        if (ObjectUtils.isEmpty(str)) {
            return returnLong;
        }
        long i = 1;

        str = str.replaceAll("十", "0");

        int index = str.indexOf("亿");
        if (index > -1) {
            str = str.replaceAll("亿", "");
            i *= 1000000000;
            str = str.substring(0, index) + str.substring(index, str.length());
        }

        index = str.indexOf("万");
        if (index > -1) {
            str = str.replaceAll("万", "");
            i *= 100000;
            str = str.substring(0, index) + str.substring(index, str.length());
        }

        index = str.indexOf("十");
        if (index > -1) {
            str = str.replaceAll("十", "");
            i *= 10;
            str = str.substring(0, index) + str.substring(index, str.length());
        }


        index = str.indexOf(".");
        if (index > -1) {
            BigDecimal returnNum = new BigDecimal(str);
            returnNum = returnNum.multiply(BigDecimal.valueOf(i));
            returnLong = returnNum.longValue();
        } else {
            returnLong = Long.parseLong(str) * i;
        }

        return returnLong;

    }

    public static boolean contains(String str, String indexOfStr) {
        return -1 < str.indexOf(indexOfStr);
    }


    //"^\\d+$"　　//非负整数（正整数   +   0）
    //  "^[0-9]*[1-9][0-9]*$"　　//正整数
    //  "^((-\\d+)|(0+))$"　　//非正整数（负整数   +   0）
    //  "^-[0-9]*[1-9][0-9]*$"　　//负整数
    //  "^-?\\d+$"　　　　//整数
    //  "^\\d+(\\.\\d+)?$"　　//非负浮点数（正浮点数   +   0）
    //  "^(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*))$"　　//正浮点数
    //  "^((-\\d+(\\.\\d+)?)|(0+(\\.0+)?))$"　　//非正浮点数（负浮点数   +   0）
    //  "^(-(([0-9]+\\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\\.[0-9]+)|([0-9]*[1-9][0-9]*)))$"　　//负浮点数
    //  "^(-?\\d+)(\\.\\d+)?$"　　//浮点数

    static Map<String,String> map = new HashMap<String,String>(){{
        put("isInteger", "^-?\\d+$");
        put("isJSONArray", "^\\[((\\d,)*\\d)*\\]$\\[((\\d,)*\\d)*\\]");//^\[(\{[^\:]*\:[^\,]*\})*\]
    }};

    /**
     *
     * @param key
     * @param content
     * @return
     */
    public static boolean valueVerification(String key, String content){
        return Pattern.matches(map.get(key), content);
    }

    public static Integer toInt(String value, Integer defaultValue) throws MsgException {
        if(ObjectUtils.notIsEmpty(value)){
            return toInt(value);
        }
        return defaultValue;
    }



    public static Integer toInt(String value) throws MsgException {
        if (valueVerification("isInteger", value)) {
            return Integer.parseInt(value);
        }
        throw new MsgException("不是数字");
    }

    //        result = result.replaceAll("\\n|\\t","");



    public void sort(){
        String ip_str = "192.168.10.34 127.0.0.1 3.3.3.3 105.70.11.55";

        // 为了让IP可以按字符串顺序比较，每一位需要0补充
        ip_str = ip_str.replaceAll("(\\d+)", "00$1");
        System.out.println(ip_str);

        // 然后每一个保留3位
        ip_str = ip_str.replaceAll("0*(\\d{3})", "$1");
        System.out.println(ip_str);

        // IP地址分割并排序
        String[] ips = ip_str.split(" +");
        Set<String> set = new TreeSet<String>();
        for (String ip : ips) {
            set.add(ip);
        }

        for (String ip : set) {
            System.out.println(ip.replaceAll("0*(\\d+)", "$1"));
        }
    }

    /**
     * 平均分配分割
     * @param data
     * @param n
     * @return
     */
    public static List averageAssignPartition(List<?> data, int n) {
//        List<List<?>> datas = new ArrayList<>();
        List<Object> datas = new ArrayList<>();
        if (ObjectUtils.notIsEmpty(data)) {
            int average = data.size() / n;
            int surplus = data.size() % n;
            Map<Integer, Integer> itemSSize = new HashMap<>();
            for (int i = 0; i < n; i++) {
                itemSSize.put(i, average);
            }
            while (surplus > 0) {
//                System.out.println(String.formatKV("[while]%d", surplus));
                for (int i = 0; i < n; i++) {
                    if (surplus > 0) {
//                        System.out.println(String.formatKV("[for]%d", surplus));
                        itemSSize.put(i, itemSSize.get(i) + 1);
                        surplus--;
                    } else {
                        break;
                    }
                }
            }
            int curNum = 0;
            for (int i = 0; i < n; i++) {
                datas.add(data.subList(curNum, curNum + itemSSize.get(i)));
                curNum += itemSSize.get(i);
            }
        }

        return datas;
    }



}
