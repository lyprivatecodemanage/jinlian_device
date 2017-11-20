/**
 * Copyright (C), 2015-2017, 上海金念有限公司
 * FileName: UrlUtil
 * Author:   liuguanglong
 * Date:     2017/11/6 16:29
 * Description: URL编码工具
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.common.utils;

/**
 * 〈一句话功能简述〉<br> 
 * 〈URL编码工具〉
 *
 * @author liuguanglong
 * @create 2017/11/6
 * @since 1.0.0
 */

import java.io.UnsupportedEncodingException;
import java.util.Date;

public class UrlUtil {
    private final static String ENCODE = "UTF-8";
    /**
     * URL 解码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:09:51
     */
    public static String getURLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     * URL 转码
     *
     * @return String
     * @author lifq
     * @date 2015-3-17 下午04:10:28
     */
    public static String getURLEncoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLEncoder.encode(str, ENCODE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @return void
     * @author lifq
     * @date 2015-3-17 下午04:09:16
     */
    public static void main(String[] args) {
//        String str = "------------heartbeatData=%7B%22MD5Check%22%3A%2272681F38AD04AE665FE84F17CE3391EF%22%2C%22command%22%3A%7B%22ACTION%22%3A%22UPLOAD_DEVICE_HEARTBEAT%22%2C%22ACTIONCode%22%3A%221005%22%2C%22subCMDID%22%3A%221%22%2C%22superCMDID%22%3A%221%22%7D%2C%22commandIndex%22%3A%221%22%2C%22commandMode%22%3A%22C%22%2C%22commandTotal%22%3A%221%22%2C%22commandType%22%3A%22S%22%2C%22data%22%3A%7B%22heartbeat%22%3A%7B%22CPUUnilization%22%3A%2222%25%22%2C%22CPUUserUnilization%22%3A%2254%25%22%2C%22IP%22%3A%220.0.0.0%22%2C%22appUsed%22%3A%5B%7B%22appName%22%3A%22CPUserialport%22%2C%22cpu%22%3A%220.0%25%22%7D%2C%7B%22appName%22%3A%22CPUheart%22%2C%22cpu%22%3A%220.0%25%22%7D%2C%7B%22appName%22%3A%22CPUserialport%22%2C%22cpu%22%3A%221.3513514%25%22%7D%2C%7B%22appName%22%3A%22CPUmqdownload%22%2C%22cpu%22%3A%221.4285715%25%22%7D%2C%7B%22appName%22%3A%22CPUmqupload%22%2C%22cpu%22%3A%221.4285715%25%22%7D%2C%7B%22appName%22%3A%22CPUdevice%22%2C%22cpu%22%3A%221.4285715%25%22%7D%2C%7B%22appName%22%3A%22CPUupdateApp%22%2C%22cpu%22%3A%220.0%25%22%7D%5D%2C%22companyId%22%3A%22001%22%2C%22companyName%22%3A%22%E6%B5%8B%E8%AF%95%22%2C%22cpuFreq%22%3A%22396000%22%2C%22cpuTemper%22%3A%2280468%22%2C%22dataUploadstate%22%3A%2210%22%2C%22deviceId%22%3A%220f1a21d4e6fd3cb8%22%2C%22doorAlarm%22%3A%220%22%2C%22fireAlarm%22%3A%220%22%2C%22gate%22%3A%220.0.0.0%22%2C%22internalUnilization%22%3A%2221.13%25%22%2C%22keySwitch%22%3A%220%22%2C%22lockState%22%3A%220%22%2C%22mask%22%3A%220.0.0.0%22%2C%22romAvailableSize%22%3A%224487163904%22%2C%22timeLimitDoorOpen%22%3A%223%22%2C%22timeLimitLockOpen%22%3A%223%22%2C%22userNumber%22%3A%221%22%2C%22wifiOpne%22%3A%220%22%7D%7D%2C%22deviceId%22%3A%220f1a21d4e6fd3cb8%22%2C%22fileEdition%22%3A%221%22%2C%22outOfTime%22%3A%22%22%2C%22sendTime%22%3A%222017-11-06+16%3A46%3A16%22%2C%22serverId%22%3A%220%22%7D\n";
//        System.out.println(getURLEncoderString(str));
//        System.out.println(getURLDecoderString(str));

        Date date = new Date();
        System.out.println("转换前："+DateUtils.formatDate(date, "yyyy-MM-dd HH:mm:ss"));
        Date dateTrans= DateUtils.addSeconds(new Date(), 60);
        System.out.println("转换后："+DateUtils.formatDate(dateTrans, "yyyy-MM-dd HH:mm"));

    }

}