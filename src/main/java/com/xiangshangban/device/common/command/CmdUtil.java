/**
 * Copyright (C), 2015-2018, 上海金念有限公司
 * FileName: CmdUtil
 * Author:   liuguanglong
 * Date:     2018/1/10 13:23
 * Description: 根据设备和后台通讯协议封装的工具类
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.common.command;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 〈一句话功能简述〉<br> 
 * 〈根据设备和后台通讯协议封装的工具类〉
 *
 * @author liuguanglong
 * @create 2018/1/10
 * @since 1.0.0
 */

@Component
public class CmdUtil {

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

    @Autowired
    private IEntranceGuardService entranceGuardService;

    /**
     * 封装的下发命令方法，目前暂未用到的或不变的参数不对外开放，减少无谓的传参量
     *
     * @param deviceId              要下发当前命令到某一台设备的设备id
     * @param commandMode           协议里commandMode部分，标识命令的发送和回复类型，目前C是主动发出，R是被动回复，data部分为空时用NULLDATA，它有特殊处理
     * @param action                协议里action部分，标识命令的类型
     * @param actionCode            协议里actionCode部分，同样也是标识命令的类型
     * @param operatorEmployeeId    下发命令的操作人，web界面当前登录人，目前部分命令需要记录操作人
     * @param dataName              协议里data部分的名称，根据不同的命令，名称各不相同
     * @param dataObject            协议里data部分的所有内容
     * @param status                命令下发时初始化的状态，目前1是立即下发命令，0是存为草稿不下发命令（存为草稿只有人员有）
     * @param employeeId            人员相关的命令需要存储一下这个人的id，即每个人一条命令，不能一条对应所有
     * @param customTimeoutSeconds  自定义超时秒数(区别于application.properties里的command.timeout.seconds全局配置)
     */
    public Map<String, Object> handOutCmd(String deviceId, String commandMode, String action,
                           String actionCode, String operatorEmployeeId, String dataName,
                           Object dataObject, String status, String resultCode, String resultMessage,
                           String employeeId, String customTimeoutSeconds, String sendTime){

        String timeoutSeconds = commandTimeoutSeconds;
        //支持自定义超时秒数(区别于application.properties里的command.timeout.seconds全局配置)
        if (StringUtils.isNotEmpty(customTimeoutSeconds)){
            timeoutSeconds = customTimeoutSeconds;
        }

        //构造命令格式
        DoorCmd doorCmd = new DoorCmd();
        doorCmd.setServerId(serverId);
        doorCmd.setDeviceId(deviceId);
        doorCmd.setFileEdition("v1.3");
        if ("NULLDATA".equals(commandMode)){
            doorCmd.setCommandMode("C");
        }else {
            doorCmd.setCommandMode(commandMode);
        }
        doorCmd.setCommandType("single");
        doorCmd.setCommandTotal("1");
        doorCmd.setCommandIndex("1");
        doorCmd.setSubCmdId("");
        doorCmd.setAction(action);
        doorCmd.setActionCode(actionCode);
        doorCmd.setEmployeeId(employeeId);
        doorCmd.setOperateEmployeeId(operatorEmployeeId);

        doorCmd.setSendTime(sendTime);
        doorCmd.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), timeoutSeconds));
        doorCmd.setSuperCmdId(FormatUtil.createUuid());
        doorCmd.setData(JSON.toJSONString(dataObject));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> completeCmdMap = messagePackaging(doorCmd, dataName, dataObject, commandMode);
        //命令状态设置为: 发送中
        doorCmd.setStatus(status);
        //设置md5校验值
        doorCmd.setMd5Check((String) completeCmdMap.get("MD5Check"));
        //如命令是回复的，即commandMode为R，则保存回复时的状态码和状态消息
        if ("R".equals(commandMode)){
            doorCmd.setResultCode(resultCode);
            doorCmd.setResultMessage(resultMessage);
            //设置数据库的data字段
            doorCmd.setData(JSON.toJSONString(completeCmdMap.get("result")));
        }else if ("C".equals(commandMode) || "NULLDATA".equals(commandMode)){
            //设置数据库的data字段
            doorCmd.setData(JSON.toJSONString(completeCmdMap.get("data")));
        }
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmd);
        //1是下发，5是回复
        if ("1".equals(status) || "5".equals(status)){
            //立即下发数据到MQ
            rabbitMQSender.sendMessage(deviceId, completeCmdMap);
        }

        return completeCmdMap;
    }

    /**
     * 封装协议数据格式
     * @param doorCmd
     * @param dataName
     * @param dataObject
     * @return
     */
    public static Map<String, Object> messagePackaging(DoorCmd doorCmd, String dataName, Object dataObject, String commandMode){

        //最外层协议格式
        Map protocolMap = new LinkedHashMap();
        //命令格式，协议格式的一部分
        Map commandMap = new LinkedHashMap();
        //携带的业务数据
        Map mapData = new LinkedHashMap();

        protocolMap.put("commandIndex", doorCmd.getCommandIndex());
        protocolMap.put("commandMode", doorCmd.getCommandMode());
        protocolMap.put("commandTotal", doorCmd.getCommandTotal());
        protocolMap.put("commandType", doorCmd.getCommandType());

        //判断是回复还是主动下发，外加data部分空数据
        if (commandMode.equals("C")){
            mapData.put(dataName, dataObject);
            protocolMap.put("data", mapData);
        }else if (commandMode.equals("R")){
            protocolMap.put("result", dataObject);
        }else if (commandMode.equals("NULLDATA")){
            protocolMap.put("data", mapData);
        }

        protocolMap.put("deviceId", doorCmd.getDeviceId());
        protocolMap.put("fileEdition", doorCmd.getFileEdition());
        protocolMap.put("outOfTime", doorCmd.getOutOfTime());
        protocolMap.put("sendTime", doorCmd.getSendTime());
        protocolMap.put("serverId", doorCmd.getServerId());
//        protocolMap.put("status", doorCmd.getStatus());

        commandMap.put("ACTION", doorCmd.getAction());
        commandMap.put("ACTIONCode", doorCmd.getActionCode());
        commandMap.put("subCMDID", doorCmd.getSubCmdId());
        commandMap.put("superCMDID", doorCmd.getSuperCmdId());

        protocolMap.put("command", commandMap);

        String md5check = JSON.toJSONString(protocolMap);
//        System.out.println(md5check);
        String md5 = "";
        try {
            md5 = MD5Util.encryptPassword(md5check, "XC9EO5GKOIVRMBQ2YE8X");
        } catch (Exception e) {
            System.out.println("md5值 = " + md5);
            System.out.println("数据MD5出错:" + e.getMessage());
        }

        protocolMap.put("MD5Check", md5);

        return protocolMap;
    }

}