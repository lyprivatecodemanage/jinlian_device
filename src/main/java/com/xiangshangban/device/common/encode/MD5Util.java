package com.xiangshangban.device.common.encode;

/**
 * Created by liuguanglong on 2017/10/24.
 */

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

        String test = "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 mDZiJw0=\n";
        //        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(test);
//        System.out.println(JSON.toJSONString(jsonObject));
//        System.out.println(encryptPassword(JSON.toJSONString(jsonObject), "XC9EO5GKOIVRMBQ2YE8X"));
//        String test = "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 WSs4gCE=\n";
        System.out.println(encryptPassword(test, "XC9EO5GKOIVRMBQ2YE8X"));

    }
}
