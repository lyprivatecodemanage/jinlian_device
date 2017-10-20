package com.xiangshangban.device.common.rmq;

import com.rabbitmq.client.*;
import com.xiangshangban.device.bean.MQMessage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class RabbitMQReciever {

//    private static final Log LOG = LogFactory.getLog(RabbitMQReciever.class);

//    @Resource
//    DeviceService deviceService;
//    @Resource
//    MessageService messageService;

    public void handleMessage(Object message) throws IOException {
        MQMessage rmqMessage = (MQMessage)message;
        if( null != rmqMessage && StringUtils.isNotEmpty(rmqMessage.getFileContent())){
            String SN = rmqMessage.getDeviceId();
            //设备有效
            if(StringUtils.isNotEmpty(SN)){
            }else{
                System.out.println("RABBITMQ'S ERROR:can not get a valid device:" + rmqMessage.getDeviceId());
            }
        }else{
            System.out.println("RABBITMQ'S ERROR:the message is null.");
        }
    }

    //接收测试
    //接收可自动创建交换器、队列并绑定，发生不能
    public static void main(String [] s) throws InterruptedException, IOException, TimeoutException {

        //交换器名称
        String EXCHANGE_NAME = "download";
        //队列名称
        String QUEUE_NAME = "hello";
        //路由关键字
        String routingKey = "hello";
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
        channel.exchangeDeclare(EXCHANGE_NAME, "topic");

        //绑定路由关键字
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, routingKey);
        System.out.println(" [*] 绑定 交换器 ["+EXCHANGE_NAME+"] 到 ["+QUEUE_NAME+"] 队列通过 routingKey [" + routingKey + "]");

        System.out.println(" [*] 消息监听中... To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + envelope.getRoutingKey() + "':'" + message + "'");
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
