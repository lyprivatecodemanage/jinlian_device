package com.xiangshangban.device.common.rmq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.dao.DoorCmdMapper;
import com.xiangshangban.device.service.IDeviceService;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by liuguanglong on 2017/10/18.
 */

@Component
public class RabbitMQReciever {

    //application.properties属性配置引入
    @Value("${rabbitmq.common.host.name}")
    String commonHostName;

    @Value("${rabbitmq.common.port.name}")
    String commonPortName;

    @Value("${rabbitmq.common.user.name}")
    String commonUserName;

    @Value("${rabbitmq.common.user.password}")
    String commonUserPassword;

    @Value("${rabbitmq.upload.exchange.name}")
    String uploadExchangeName;

    @Value("${rabbitmq.upload.queue.name}")
    String uploadQueueName;

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IEntranceGuardService entranceGuardService;

    @Autowired
    private DeviceMapper deviceMapper;

    //接收测试
    //接收可自动创建交换器、队列并绑定，发出不能
    public void startRabbitMqReceiver() throws InterruptedException, IOException, TimeoutException {

        //交换器名称
        String EXCHANGE_NAME = uploadExchangeName;
        //队列名称
        String QUEUE_NAME = uploadQueueName;
        //路由关键字
        String routingKey = uploadQueueName;
        //主机ip
        String host = commonHostName;
        //rabbitMQ端口号
        int port = Integer.parseInt(commonPortName);
        //rabbitMQ用户名
        String userName = commonUserName;
        //rabbitMQ密码
        String userPassword = commonUserPassword;

        ConnectionFactory connectionFactory = new ConnectionFactory();

        //设置主机ip
        connectionFactory.setHost(host);
        //设置端口(rabbitMQ端口)
        connectionFactory.setPort(port);
        //设置用户名
        connectionFactory.setUsername(userName);
        //设置密码
        connectionFactory.setPassword(userPassword);

        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        //RabbitMQ客户端接受消息最大数量
        channel.basicQos(0,1,false);

        //声明一个匹配模式的交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

        //绑定路由关键字
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, routingKey);

        System.out.println(" [*] 绑定 交换器 ["+EXCHANGE_NAME+"] 到 ["+QUEUE_NAME+"] 队列通过 routingKey [" + routingKey + "]");

//        System.out.println(" [*] 消息监听中... To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            @Transactional
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject = JSONObject.fromObject(message);

                    Map<String, Object> mapResult = (Map<String, Object>) jsonObject;
                    String deviceId = (String) mapResult.get("deviceId");

                    //判断是设备主动上传还是消息回复
                    if (mapResult.get("commandMode").equals("C")){
                        //md5校验
                        //获取对方的md5
                        String otherMd5 = (String) mapResult.get("MD5Check");
                        mapResult.remove("MD5Check");
                        String messageCheck = JSON.toJSONString(mapResult);
//                    System.out.println("去MD5后的数据 : " + messageCheck);
                        //生成我的md5
                        String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
//                    System.out.println("我的MD5 = " + myMd5);
                        //双方的md5比较判断
                        if (myMd5.equals(otherMd5)){
//                            System.out.println("MD5校验成功，数据完好无损");

                            //CRC16校验deviceId
                            if (deviceService.checkCrc16DeviceId(deviceId)){

                                //命令类型判断
                                Map<String, String> commandMap = (Map<String, String>)mapResult.get("command");
                                if (commandMap.get("ACTION").equals("UPLOAD_ACCESS_RECORD")){
                                    //门禁记录上传存储（RabbitMQ 上传）
                                    entranceGuardService.doorRecordSave(message);
                                }else if (commandMap.get("ACTION").equals("UPLOAD_DEVICE_REBOOT_RECORD")){
                                    //设备重启记录上传存储
                                    deviceService.deviceRebootRecordSave(JSON.toJSONString(mapResult.get("data")), deviceId);
                                }else if (commandMap.get("ACTION").equals("UPLOAD_DEVICE_RUNNING_LOG")){
                                    //设备运行日志上传存储
                                    deviceService.deviceRunningLogSave(JSON.toJSONString(mapResult.get("data")), deviceId);
                                }

                            }

                        }else {
                            System.out.println("我的MD5 = " + myMd5);
                            System.out.println("MD5校验失败，数据已被修改");
                        }

                    }else if (mapResult.get("commandMode").equals("R")){
                        //返回值为0代表执行成功
                        if (((Map<String, String>)mapResult.get("resultData")).get("resultCode").equals("0")){

                            //设备回复的命令获取superCMDID
                            Map<String, String> commandMap = (Map<String, String>)mapResult.get("command");
                            String superCmdId = commandMap.get("superCMDID");
                            DoorCmd doorCmd = new DoorCmd();
                            doorCmd.setSuperCmdId(superCmdId);

                            //删除人员的命令收到回复时将命令状态置为4：已删除，而不是2：下发成功，其它都置为下发成功
                            DoorCmd doorCmdTemp = doorCmdMapper.selectBySuperCmdId(superCmdId);
                            if (doorCmdTemp.getAction().equals("DELETE_USER_INFO")){
                                doorCmd.setStatus("4");
                            }else {
                                if (doorCmdTemp.getAction().equals("UNBIND_DEVICE")){
                                    //同步门上的信息
                                    List returnObj = (List)((Map<String, Object>)mapResult.get("resultData")).get("returnObj");
                                    Map<String, String> returnMap = (Map<String, String>) (returnObj.get(0));
                                    String unbindType = returnMap.get("unbindType");
                                    String newDeviceId = returnMap.get("newDeviceId");
                                    String doorId = returnMap.get("doorId");
                                    String operatorEmployeeId = returnMap.get("operatorEmployeeId");
                                    if ("0".equals(unbindType)){
                                        //执行设备和门解绑的，同步门上信息到新设备的操作
                                        deviceService.synchronizationDoorInfoToNewDevice(doorId, newDeviceId, operatorEmployeeId);
                                    }else if ("1".equals(unbindType)){
                                        //执行设备和公司解绑的删除操作
                                        deviceService.unBindDeviceDeleteOperation(deviceId);
                                    }

                                    //改变设备的绑定状态为未绑定，即解绑成功
                                    Device device = new Device();
                                    device.setDeviceId(deviceId);
                                    device.setIsUnbind("1");
                                    deviceMapper.updateByPrimaryKeySelective(device);
                                }
                                doorCmd.setStatus("2");
                            }
                            //改变这条命令的状态
                            doorCmdMapper.updateBySuperCmdIdSelective(doorCmd);
                        }else if (((Map<String, String>)mapResult.get("resultData")).get("resultCode").equals("9")){
                            //设备回复的命令获取superCMDID
                            Map<String, String> commandMap = (Map<String, String>)mapResult.get("command");
                            String superCmdId = commandMap.get("superCMDID");
                            DoorCmd doorCmd = new DoorCmd();
                            doorCmd.setSuperCmdId(superCmdId);

                            DoorCmd doorCmdTemp = doorCmdMapper.selectBySuperCmdId(superCmdId);

//                            System.out.println("*****"+JSON.toJSONString(doorCmdTemp));
                            if (doorCmdTemp.getAction().equals("UNBIND_DEVICE")){
                                //设备端有尚未上传的数据，改变设备的绑定状态为数据未完全上传
                                Device device = new Device();
                                device.setDeviceId(deviceId);
                                device.setIsUnbind("4");
                                deviceMapper.updateByPrimaryKeySelective(device);
                            }
                            doorCmd.setStatus("2");

                            //改变这条命令的状态
                            doorCmdMapper.updateBySuperCmdIdSelective(doorCmd);
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    System.out.println("RabbitMQ收到非法JSON数据！！！");
                    return;
                }
            }
        };

        channel.basicConsume(QUEUE_NAME, true, consumer);

//        QueueingConsumer consumer = new QueueingConsumer(channel);
//        channel.basicConsume(QUEUE_NAME, true, consumer);
//
//        while (true) {
//            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
//            String message = new String(delivery.getBody());
//            System.out.println(" [x] Received '" + message + "'");
//        }
    }
}
