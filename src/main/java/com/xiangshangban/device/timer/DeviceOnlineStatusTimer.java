/**
 * Copyright (C), 2015-2017, 上海金念有限公司
 * FileName: DeviceOnlineStatusTimer
 * Author:   liuguanglong
 * Date:     2017/12/11 14:58
 * Description: 设备在线状态检测
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.timer;

import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.common.utils.DeviceStatusCheckUtil;
import com.xiangshangban.device.dao.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 〈一句话功能简述〉<br> 
 * 〈设备在线状态检测〉
 *
 * @author liuguanglong
 * @create 2017/12/11
 * @since 1.0.0
 */

@Component
public class DeviceOnlineStatusTimer {

    @Autowired
    private DeviceStatusCheckUtil deviceStatusCheckUtil;

    @Autowired
    private DeviceMapper deviceMapper;

    //每两分钟检查一次
    public final static String DEVICE_ONLINE_CHECK_TIME = "0 0/2 * * * ?";
//    public final static String DEVICE_ONLINE_CHECK_TIME = "5 * * * * ?";

    @Scheduled(cron = DEVICE_ONLINE_CHECK_TIME)
    public void check() {

//        System.out.println("hello!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//        System.out.println("****************************开始检测所有设备的在线状态****************************");

        //检查所有设备是否在线
        List<Device> deviceList = deviceMapper.selectAllDeviceInfoByNone();
        for (Device device : deviceList) {
            //检测心跳是否在线
            deviceStatusCheckUtil.deviceStatusChecking(device.getDeviceId());
            //设备心跳在线状态检测任务（输出到独立的心跳时间区间表，以便前端渲染在线状态波形图）
            deviceStatusCheckUtil.deviceOnlineChecking(device.getDeviceId());
        }
//        System.out.println("****************************设备在线状态检测结束****************************");
    }

}