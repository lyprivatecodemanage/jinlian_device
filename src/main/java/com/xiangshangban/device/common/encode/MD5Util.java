package com.xiangshangban.device.common.encode;

/**
 * Created by liuguanglong on 2017/10/24.
 */

import com.alibaba.fastjson.JSON;

import java.security.MessageDigest;

public class MD5Util {
    // 公盐
    private static final String PUBLIC_SALT = "demo";
    // 十六进制下数字到字符的映射数组
    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"};

    /**
     * 用户密码加密，盐值为 ：私盐+公盐
     *
     * @param password 密码
     * @param salt     私盐
     * @return MD5加密字符串
     */
    public static String encryptPassword(String password, String salt) {
        return encodeByMD5(PUBLIC_SALT + password + salt);
    }

    /**
     * md5加密算法
     *
     * @param originString
     * @return
     */
    private static String encodeByMD5(String originString) {
        if (originString != null) {
            try {
                // 创建具有指定算法名称的信息摘要
                MessageDigest md = MessageDigest.getInstance("MD5");
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md.digest(originString.getBytes());
                // 将得到的字节数组变成字符串返回
                String resultString = byteArrayToHexString(results);
                return resultString.toUpperCase();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 转换字节数组为十六进制字符串
     *
     * @param
     * @return 十六进制字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    /**
     * 将一个字节转化成十六进制形式的字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static void main(String[] args) {

        String test = "{ \"commandIndex\": \"1\", \"commandMode\": \"C\", \"commandTotal\": \"1\", \"commandType\": \"S\", \"deviceId\": \"0f1a21d4e6fd3cb8\", \"fileEdition\": \"1\", \"outOfTime\": \"\", \"sendTime\": \"2017-11-08 16:36:10\", \"serverId\": \"0\", \"command\": { \"ACTION\": \"UPLOAD_DEVICE_HEARTBEAT\", \"ACTIONCode\": \"1004\", \"subCMDID\": \"1\", \"superCMDID\": \"1\" }, \"data\": { \"record\": [ { \"deviceId\": \"0f1a21d4e6fd3cb8\", \"rebooUpload\": \"0\", \"rebootId\": \"1c47ea93ee534e28b61f8bdd323457a1\", \"rebootNumber\": \"4\", \"rebootTime\": \"2017-11-07 16:13:39\", \"version\": [ { \"name\": \"systemVersion\", \"value\": \"5.1.1\" }, { \"name\": \"bleModeVersion\", \"value\": \"20170928v0004\" } ] }, { \"deviceId\": \"0f1a21d4e6fd3cb8\", \"rebooUpload\": \"0\", \"rebootId\": \"57365804bd4c47158277d2e57853c28a\", \"rebootNumber\": \"5\", \"rebootTime\": \"2017-11-08 10:29:17\", \"version\": [ { \"name\": \"systemVersion\", \"value\": \"5.1.1\" }, { \"name\": \"bleModeVersion\", \"value\": \"20170928v0004\" } ] }, { \"deviceId\": \"0f1a21d4e6fd3cb8\", \"rebooUpload\": \"0\", \"rebootId\": \"884ae828d3174b6bb6d214f43802b035\", \"rebootNumber\": \"6\", \"rebootTime\": \"2017-11-08 16:32:15\", \"version\": [ { \"name\": \"systemVersion\", \"value\": \"5.1.1\" }, { \"name\": \"bleModeVersion\", \"value\": \"20170928v0004\" } ] } ] } }";
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(test);
        System.out.println(JSON.toJSONString(jsonObject));
        System.out.println(encryptPassword(JSON.toJSONString(jsonObject), "XC9EO5GKOIVRMBQ2YE8X"));

    }
}
