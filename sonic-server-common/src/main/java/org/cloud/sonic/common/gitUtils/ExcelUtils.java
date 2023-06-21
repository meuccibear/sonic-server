package org.cloud.sonic.common.gitUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.cloud.sonic.common.gitUtils.exception.MsgException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: HNTD
 * @ClassName Excel
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-02-22 11:27
 * @Version 1.0
 **/
public class ExcelUtils {



    public static void main(String[] args) throws MsgException {
//        List<JSONObject> jsonArray = readFile("./devices3.txt");
//
//        jsonArray.forEach(jsonO -> {
//            JSONObject jsonObject = (JSONObject) jsonO;
////            getHotspotsByAddress();
//            System.out.println(jsonObject.get("address"));
//        });


        System.out.println(System.getProperty("user.dir"));
//        System.out.println(toTempStr("0000000000000000", "100"));

    }



    public static String toTempStr(String tempStr, String str) {
        int num = tempStr.length() - str.length();
        return tempStr.substring(0, num) + str;
    }

    public static String toTempStr1(String tempStr, int num) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < num; i++) {
            sb.append(tempStr);
        }
        return sb.toString();
    }



    public static List<JSONObject> readTxt(String path) throws MsgException {
        String [] strings = path.split("\n");

        JSONObject jsonObject;
        List<JSONObject> jsonArray = new ArrayList<>();
        String tempStr;
        String[] cols = new String[0];
        String[] vals;
        for (int i = 0; i < strings.length; i++) {
            tempStr = strings[i];
            System.out.println(tempStr);
            if (ObjectUtils.notIsEmpty(tempStr)) {
                if (0 == i) {
                    cols = tempStr.split("\t");
                } else {
                    vals = tempStr.split("\t");
                    jsonObject = new JSONObject();
                    System.out.println(JSON.toJSONString(cols));

                    for (int i1 = 0; i1 < cols.length; i1++) {
                        if(i1<vals.length){
                            jsonObject.put(cols[i1], vals[i1]);
                        }
                    }
                    jsonArray.add(jsonObject);
                }
            }
        }
        return jsonArray;
    }
}
