package com.xiangshangban.device.common.rmq;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.*;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.bean.MQMessage;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.dao.DoorCmdMapper;
import com.xiangshangban.device.service.IEmployeeService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.stereotype.Component;
//import com.rabbitmq.config.AmqpConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by liuguanglong on 2017/10/18.
 */

@Component
public class RabbitMQReciever {

    @Autowired
    private IEmployeeService employeeService;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    //接收测试
    //接收可自动创建交换器、队列并绑定，发出不能
    public void startRabbitMqReceiver() throws InterruptedException, IOException, TimeoutException {

        //交换器名称
        String EXCHANGE_NAME = "upload";
        //队列名称
        String QUEUE_NAME = "welcome";
        //路由关键字
        String routingKey = "welcome";
        //主机ip
        String host = "localhost";
        //rabbitMQ端口号
        int port = 5672;
        //rabbitMQ用户名
        String userName = "test";
        //rabbitMQ密码
        String userPassword = "123";

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

        //声明一个匹配模式的交换器
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);

        //绑定路由关键字
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, routingKey);
        System.out.println(" [*] 绑定 交换器 ["+EXCHANGE_NAME+"] 到 ["+QUEUE_NAME+"] 队列通过 routingKey [" + routingKey + "]");

        System.out.println(" [*] 消息监听中... To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject = JSONObject.fromObject(message);
                }catch (Exception e){
//                    e.printStackTrace();
                    System.out.println("RabbitMQ收到非法JSON数据！！！");
                    return;
                }

                Map<String, Object> mapResult = (Map<String, Object>) jsonObject;
                //判断是设备主动上传还是消息回复
                if (mapResult.get("commandMode").equals("C")){
                    //md5校验
                    //获取对方的md5
                    String otherMd5 = (String) mapResult.get("MD5Check");
                    mapResult.remove("MD5Check");
                    String messageCheck = JSON.toJSONString(mapResult);
                    //生成我的md5
                    String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
                    //双方的md5比较判断
                    if (myMd5.equals(otherMd5)){
                        System.out.println("设备上传的数据未被修改");
                    }else {
                        System.out.println("设备上传的数据已被修改");
                    }

                    //命令类型判断
                    Map<String, String> commandMap = (Map<String, String>)mapResult.get("command");
                    if (commandMap.get("ACTION").equals("UPDATE_USER_LABEL")){
                        //人员人脸、指纹、卡号信息上传存储
                        employeeService.saveEmployeeInputInfo((String) mapResult.get("data"));
                    }else if (commandMap.get("ACTION").equals("UPLOAD_ACCESS_RECORD")){
                        //门禁记录上传存储
                        System.out.println("[#] message: " + message);
                        employeeService.doorRecordSave(message);
                    }

                }else if (mapResult.get("commandMode").equals("R")){
                    //回复的数据获取subCMDID
                    Map<String, String> commandMap = (Map<String, String>)mapResult.get("command");
                    String subCmdId = commandMap.get("subCMDID");
                    DoorCmd doorCmd = new DoorCmd();
                    doorCmd.setSubCmdId(subCmdId);
                    doorCmd.setStatus("2");
                    //改变该这条命令的状态
                    doorCmdMapper.updateBySubCmdIdSelective(doorCmd);
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
