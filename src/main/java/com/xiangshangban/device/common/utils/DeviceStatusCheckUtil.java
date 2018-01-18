/**
 * Copyright (C), 2015-2018, 上海金念有限公司
 * FileName: DeviceStatusCheckUtil
 * Author:   liuguanglong
 * Date:     2018/1/17 13:40
 * Description: 设备在线状态检测工具类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.common.utils;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.bean.DeviceOnline;
import com.xiangshangban.device.dao.DeviceHeartbeatMapper;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.dao.DeviceOnlineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈设备在线状态检测工具类〉
 *
 * @author liuguanglong
 * @create 2018/1/17
 * @since 1.0.0
 */
@Component
public class DeviceStatusCheckUtil {

    @Autowired
    private DeviceHeartbeatMapper deviceHeartbeatMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceOnlineMapper deviceOnlineMapper;

    public String deviceStatusChecking(String deviceId){
        //在线状态，1在线，0离线
        String onlineStatus = "";

        List<Map<String, String>> deviceHeartbeatLatestTimeList = deviceHeartbeatMapper.selectLatestTimeByDeviceId(deviceId);
        String deviceHeartbeatLatestTime = "";
        try {
            Device deviceTemp = new Device();
            deviceTemp.setDeviceId(deviceId);

            if (null != deviceHeartbeatLatestTimeList && deviceHeartbeatLatestTimeList.size() == 1){
                deviceHeartbeatLatestTime = deviceHeartbeatLatestTimeList.get(0).get("time");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date deviceHeartBeatLatestDate = sdf.parse(deviceHeartbeatLatestTime);
                Date dateNow = new Date();
                //最新的一条心跳更新时间和当前时间的毫秒数间隔
                Long intervalMillisecond = dateNow.getTime() - deviceHeartBeatLatestDate.getTime();

//                    System.out.println("intervalMillisecond:  "+intervalMillisecond);

                //以大于1.5分钟没有新的心跳信息为离线状态来检测
                if (intervalMillisecond < 90000){
                    onlineStatus = "1";
                    deviceTemp.setIsOnline("1");//设备在线
                    deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                        System.out.println("【"+device.getDeviceId()+"】设备在线中.......................");
                }else {
                    onlineStatus = "0";
                    deviceTemp.setIsOnline("0");//设备离线
                    deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                        System.out.println("【"+device.getDeviceId()+"】设备不在线");
                }
            }else {
                //一条心跳信息都没有的设备也是离线状态
                onlineStatus = "0";
                deviceTemp.setIsOnline("0");//设备离线
                deviceMapper.updateByPrimaryKeySelective(deviceTemp);
//                    System.out.println("【"+device.getDeviceId()+"】设备不在线");
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("设备在线状态检查出错");
        }

        return onlineStatus;
    }

    public void deviceOnlineChecking(String deviceId){
        //查询最后一条心跳的时间
        List<Map<String, String>> deviceHeartbeatLatestTimeList = deviceHeartbeatMapper.selectLatestTimeByDeviceId(deviceId);
        //必须要有至少一条心跳信息
        if (null != deviceHeartbeatLatestTimeList && deviceHeartbeatLatestTimeList.size() == 1){
            //最后一条心跳信息的时间
            String latestTime = deviceHeartbeatLatestTimeList.get(0).get("time");

            //设备心跳在线状态检测任务
            String onlineStatus = deviceStatusChecking(deviceId);
            List<DeviceOnline> deviceOnlineList =  deviceOnlineMapper.selectByPrimaryKey(deviceId);
            System.out.println("【deviceOnlineList】: "+ JSON.toJSONString(deviceOnlineList));
            if (deviceOnlineList == null || deviceOnlineList.size() == 0){
                DeviceOnline deviceOnline = new DeviceOnline();
                deviceOnline.setDeviceId(deviceId);
                deviceOnline.setStartTime(latestTime);
                deviceOnline.setIsOnline(onlineStatus);
                deviceOnlineMapper.insert(deviceOnline);
            }else {
                //查找这台设备在线状态时间区间的最大开始时间和结束时间
                String maxStartTime = deviceOnlineMapper.selectMaxStartTimeByDeviceId(deviceId);
                String maxEndTime = deviceOnlineMapper.selectMaxEndTimeByDeviceId(deviceId);
                System.out.println("【maxStartTime】: "+maxStartTime);
                System.out.println("【maxEndTime】: "+maxEndTime);
                if (maxStartTime.equals(maxEndTime)){
                    String isOnline = deviceOnlineMapper.selectByDeviceIdAndStartTime(deviceId, maxEndTime).getIsOnline();
                    if (!onlineStatus.equals(isOnline)){
                        //最新时间的那一行，把当前时间存入end_time
                        DeviceOnline deviceOnline1 = new DeviceOnline();
                        deviceOnline1.setDeviceId(deviceId);
                        deviceOnline1.setStartTime(maxStartTime);
                        deviceOnline1.setEndTime(latestTime);
                        deviceOnlineMapper.updateByDeviceIdAndStartTimeSelective(deviceOnline1);
                        //重新添加一行，把当前时间存入start_time
                        DeviceOnline deviceOnline2 = new DeviceOnline();
                        deviceOnline2.setDeviceId(deviceId);
                        deviceOnline2.setStartTime(latestTime);
                        deviceOnline2.setIsOnline(onlineStatus);
                        deviceOnlineMapper.insert(deviceOnline2);
                    }
                }else {
                    System.out.println("第二次检测");
                    String isOnline = deviceOnlineList.get(0).getIsOnline();
                    String startTime = deviceOnlineList.get(0).getStartTime();
                    System.out.println("【startTime】: "+startTime);
                    if (!onlineStatus.equals(isOnline)){
                        //刚开始只有一条时间区间记录时，把当前时间存入end_time
                        DeviceOnline deviceOnline1 = new DeviceOnline();
                        deviceOnline1.setDeviceId(deviceId);
                        deviceOnline1.setStartTime(startTime);
                        deviceOnline1.setEndTime(latestTime);
                        deviceOnlineMapper.updateByDeviceIdAndStartTimeSelective(deviceOnline1);
                        //重新添加一行，把当前时间存入start_time
                        DeviceOnline deviceOnline2 = new DeviceOnline();
                        deviceOnline2.setDeviceId(deviceId);
                        deviceOnline2.setStartTime(latestTime);
                        deviceOnline2.setIsOnline(onlineStatus);
                        deviceOnlineMapper.insert(deviceOnline2);
                    }
                }
            }
        }
    }
}