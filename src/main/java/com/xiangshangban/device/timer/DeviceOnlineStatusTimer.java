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
import com.xiangshangban.device.dao.DeviceHeartbeatMapper;
import com.xiangshangban.device.dao.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private DeviceHeartbeatMapper deviceHeartbeatMapper;

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
            List<Map<String, String>> deviceHeartbeatLatestTimeList = deviceHeartbeatMapper.selectLatestTimeByDeviceId(device.getDeviceId());
            String deviceHeartbeatLatestTime = "";
            try {
                Device deviceTemp = new Device();
                deviceTemp.setDeviceId(device.getDeviceId());

                if (null != deviceHeartbeatLatestTimeList && deviceHeartbeatLatestTimeList.size() == 1){
                    deviceHeartbeatLatestTime = deviceHeartbeatLatestTimeList.get(0).get("time");
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date deviceHeartBeatLatestDate = sdf.parse(deviceHeartbeatLatestTime);
                    Date dateNow = new Date();
                    //最新的一条心跳更新时间和当前时间的毫秒数间隔
                    Long intervalMillisecond = dateNow.getTime() - deviceHeartBeatLatestDate.getTime();

//                    System.out.println("intervalMillisecond:  "+intervalMillisecond);

                    //以大于2分钟没有新的心跳信息为离线状态来检测
                    if (intervalMillisecond < 90000){
                        deviceTemp.setIsOnline("1");//设备在线
                        deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                        System.out.println("【"+device.getDeviceId()+"】设备在线中.......................");
                    }else {
                        deviceTemp.setIsOnline("0");//设备离线
                        deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                        System.out.println("【"+device.getDeviceId()+"】设备不在线");
                    }
                }else {
                    //一条心跳信息都没有的设备也是离线状态
                    deviceTemp.setIsOnline("0");//设备离线
                    deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                    System.out.println("【"+device.getDeviceId()+"】设备不在线");
                }
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("设备在线状态检查出错");
            }
        }
//        System.out.println("****************************设备在线状态检测结束****************************");
    }

}