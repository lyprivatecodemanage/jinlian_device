package com.xiangshangban.device.timer;

import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.dao.DoorCmdMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by liuguanglong on 2017/10/28.
 */

@Component
public class DoorCmdStatusTimer {

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    //每1分钟检查一次
    public final static String DOOR_CMD_CHECK_TIME = "0 0/1 * * *  ? ";
//    public final static String DOOR_CMD_CHECK_TIME = "*/2 * * * *  ? ";

    @Scheduled(cron = DOOR_CMD_CHECK_TIME)
    public void check() {

//        System.out.println("hello!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        //检测所有处于发送中状态的命令是否超时
        List<DoorCmd> doorCmdList = doorCmdMapper.selectByStatus("1");
        for (DoorCmd doorCmd : doorCmdList) {
            String currentTime = DateUtils.getDateminutes();
            String outOfTime = doorCmd.getOutOfTime();
            if (DateUtils.isTime1LtTime2(outOfTime, currentTime)){

//                System.out.println(outOfTime);
//                System.out.println(currentTime);
//                System.out.println(doorCmd.getSubCmdId());
                //判断超时则将命令状态从发送中改为发送失败
                DoorCmd doorCmdTemp = new DoorCmd();
                doorCmdTemp.setSuperCmdId(doorCmd.getSuperCmdId());
                doorCmdTemp.setStatus("3");
                doorCmdMapper.updateBySuperCmdIdSelective(doorCmdTemp);

                //检测超时命令为解绑命令时，修改设备表的解绑状态为解绑失败（超时）
                if ("1002".equals(doorCmd.getActionCode())){
                    Device device = new Device();
                    device.setDeviceId(doorCmd.getDeviceId());
                    device.setIsUnbind("3");

                    deviceMapper.updateByPrimaryKeySelective(device);
                }
            }
        }
    }
}
