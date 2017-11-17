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

        String test = "{\"commandIndex\":\"1\",\"commandMode\":\"C\",\"commandTotal\":\"1\",\"commandType\":\"single\",\"data\":{\"templates\":[{\"alertButtonColor\":\"#385E3E\",\"imageName\":\"back\",\"imagePath\":\"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F\",\"alertImgPath\":\"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F\",\"templateId\":\"2\",\"templateType\":\"0\",\"items\":[{\"templateId\":\"2\",\"itemTopX\":\"661\",\"itemTopY\":\"370\",\"itemType\":\"1\",\"itemFontOrient\":\"1\",\"itemId\":\"44\"},{\"templateId\":\"2\",\"itemTopX\":\"596\",\"itemTopY\":\"212\",\"itemType\":\"2\",\"itemFontOrient\":\"1\",\"itemId\":\"45\"},{\"templateId\":\"2\",\"itemTopX\":\"580\",\"itemTopY\":\"180\",\"itemImgPath\":\"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F\",\"itemImgName\":\"fontFrame\",\"itemType\":\"3\",\"itemId\":\"46\"},{\"templateId\":\"2\",\"itemTopX\":\"30\",\"itemTopY\":\"802\",\"itemType\":\"4\",\"itemId\":\"47\"},{\"templateId\":\"2\",\"itemTopX\":\"30\",\"itemTopY\":\"846\",\"itemType\":\"5\",\"itemId\":\"48\"},{\"templateId\":\"2\",\"itemTopX\":\"\",\"itemTopY\":\"\",\"itemImgPath\":\"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F\",\"itemImgName\":\"bellBack\",\"itemType\":\"6\",\"itemId\":\"49\"},{\"templateId\":\"2\",\"itemTopX\":\"24\",\"itemTopY\":\"1128\",\"itemType\":\"7\",\"itemId\":\"50\"},{\"templateId\":\"2\",\"itemTopX\":\"628\",\"itemTopY\":\"1168\",\"itemType\":\"8\",\"itemId\":\"51\"},{\"itemStartDate\":\"2017-11-10 08:00\",\"templateId\":\"2\",\"itemTopX\":\"660\",\"itemFontBold\":\"1\",\"itemTopY\":\"88\",\"itemFontContent\":\"逝\",\"itemFontSize\":60.0,\"itemType\":\"0\",\"itemEndDate\":\"2017-11-10 18:00\",\"itemFontOrient\":\"0\",\"itemFontColor\":\"#FFFFFF\",\"itemId\":\"52\"},{\"itemStartDate\":\"2017-11-10 08:00\",\"templateId\":\"2\",\"itemTopX\":\"570\",\"itemFontBold\":\"1\",\"itemTopY\":\"70\",\"itemFontContent\":\"秋\",\"itemFontSize\":80.0,\"itemType\":\"0\",\"itemEndDate\":\"2017-11-10 18:00\",\"itemFontOrient\":\"0\",\"itemFontColor\":\"#FFFFFF\",\"itemId\":\"53\"}],\"templateLevel\":\"2\",\"alertImgName\":\"bellBtn\"}]},\"deviceId\":\"1\",\"fileEdition\":\"v1.3\",\"outOfTime\":\"2017-11-13 16:30\",\"sendTime\":\"2017-11-10 16:30:20\",\"serverId\":\"001\",\"command\":{\"ACTION\":\"UPDATE_DEVICE_TEMPLATE\",\"ACTIONCode\":\"4001\",\"subCMDID\":\"\",\"superCMDID\":\"9D6E53F792E24A50AF365A1C65168D20\"}}";
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(test);
        System.out.println(JSON.toJSONString(jsonObject));
        System.out.println(encryptPassword(JSON.toJSONString(jsonObject), "XC9EO5GKOIVRMBQ2YE8X"));

    }
}
