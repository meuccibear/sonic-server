package org.cloud.sonic.common.gitUtils.network;

import org.cloud.sonic.common.gitUtils.ObjectUtils;
import org.cloud.sonic.common.gitUtils.exception.MsgException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @program: helium-pro-new
 * @ClassName IPUtils
 * @description:
 * @author: Mr.Lv
 * @email: lvzhuozhuang@foxmail.com
 * @create: 2022-04-14 10:38
 * @Version 1.0
 **/
public class IPUtils {
    public static boolean pingIp(String ip) throws UnknownHostException, IOException {
        //能ping通放回true 反之 false 超时时间 3000毫秒
        return InetAddress.getByName(ip).isReachable(5000);
    }

    public static boolean telnetport(String proxyAddr) throws MsgException {
        Object[] objects = formatIP(proxyAddr);
        String ip = (String) objects[0];
        Integer port = (Integer) objects[1];

        Socket connect = new Socket();
        boolean res = false;
        try {
            connect.connect(new InetSocketAddress(ip, port), 1000);//建立连接
            //能telnet通返回true，否则返回false
            res = connect.isConnected();//通过现有方法查看连通状态
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("false");//当连不通时，直接抛异常，异常捕获即可
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("false");
            }
        }
        return res;
    }


    public static Object[] formatIP(String proxyAddr) throws MsgException {
        if (ObjectUtils.isEmpty(proxyAddr)) {
            throw new MsgException("代理地址为空！");
        }
        if (ObjectUtils.notIsEmpty(proxyAddr) && proxyAddr.contains(":")) {
            String[] datas = proxyAddr.split(":");
            Object[] result = {datas[0], Integer.parseInt(datas[1])};
            if (ObjectUtils.isEmpty(result)) {
                throw new MsgException(String.format("代理地址解析错误！|%s|", proxyAddr));
            }
            return result;
        }
        return null;
    }


//    @SneakyThrows
//    public static void main(String[] args) {
//        if(HttpUtilsx.useKDL()){
//            System.out.println(JSON.toJSONString(HttpUtilsx.get("http://42.240.155.225:8090/report.json")));
//        }
//    }

}
