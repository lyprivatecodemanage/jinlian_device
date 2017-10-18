package com.xiangshangban.common.rmq;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.bean.ConnectionUtil;
import com.xiangshangban.common.encode.DESEncode;
import com.xiangshangban.common.utils.RabbitTemplateUtil;
import com.xiangshangban.timer.ConnectionFactoryImpl;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.springframework.boot.Banner.Mode.LOG;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class RabbitMQSender {

//    private static final Log LOG = LogFactory.getLog(RabbitMQSender.class);

    private static String SUCCESS = "success";
    private static String ERROR = "error";
    /**
     * 发送消息
     * @param queueName
     * @param message
     * @throws Exception
     */
    public String sendMessage(String queueName,Object message) {

        RabbitTemplateUtil templateutil = this.getRabbitMQTemplate(queueName);
        RabbitTemplate template = templateutil.getTemplate();
        //DESEncode.encrypt(JSON.toJSONString(message));
        if(null != template){
            String json = JSON.toJSONString(message);
//            byte[] encry = null ;
//            try {
//                encry = DESEncode.encrypt(json).getBytes();
//                System.out.println("encry" + encry.toString());
//            } catch (Exception e) {
//                System.out.println("数据加密出错:"+e.getMessage());
//            }
//            template.convertAndSend(encry);
            template.convertAndSend(json);
            ConnectionFactoryImpl.destoryConnection(templateutil.getKey());
        }else{
            System.out.println("RABBITMQ'S ERROR: can not intalialize the template.");
            return RabbitMQSender.ERROR;
        }
        return RabbitMQSender.SUCCESS;
    }

//    /**
//     * 创建链接工厂
//     * @return
//     */
//    private ConnectionFactory getConnectionFactory(){
//        String host = "localhost";
//        String username = "test";
//        String password = "123";
//        int port = 5672;
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost(host);
//        connectionFactory.setPort(port);
//        connectionFactory.setUsername(username);
//        connectionFactory.setPassword(password);
//        connectionFactory.setRequestedHeartBeat(180);
//        connectionFactory.setCloseTimeout(10);
//        return connectionFactory;
//    }


    public static void main(String [] s) throws InterruptedException {
        RabbitMQSender sd = new RabbitMQSender();
//        sd.getConnectionFactory();
        while (true){
            sd.sendMessage("hello", "hello,刘文志");
            System.out.println("[send] hello,刘文志");
            Thread.sleep(5000);
        }


//        RabbitTemplate template = new RabbitTemplate();
//
//        String host = "localhost";
//        String username = "test";
//        String password = "123";
//        int port = 5672;
//        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
//        connectionFactory.setHost(host);
//        connectionFactory.setPort(port);
//        connectionFactory.setUsername(username);
//        connectionFactory.setPassword(password);
//        connectionFactory.setRequestedHeartBeat(180);
//        connectionFactory.setCloseTimeout(10);
//
//        template.setConnectionFactory(connectionFactory);
//        template.setReceiveTimeout(10);
//        template.setChannelTransacted(true);
//
//        template.setReceiveTimeout(100);
//        template.getUnconfirmed(100);
//        template.setReplyTimeout(100);
//        template.setExchange("download");
//        template.setRoutingKey("hello");
//        template.setQueue("hello");
//
//        template.convertAndSend("hello,王勇辉");
//
//        connectionFactory.destroy();
    }


    /**
     * 获取发送模板
     * @param queueName
     * @return
     */
    private  RabbitTemplateUtil getRabbitMQTemplate(String queueName) {
//        ConnectionFactory connectionFactory = this.getConnectionFactory();
        ConnectionFactoryImpl connectionFactoryImpl = new ConnectionFactoryImpl();
        ConnectionUtil conn = connectionFactoryImpl.getConnectionFactory();
        ConnectionFactory connectionFactory = conn.getConnectionFactory();
        if( null == connectionFactory) return null;
        RabbitTemplateUtil templateutil = new RabbitTemplateUtil();
        RabbitTemplate template = new RabbitTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setReceiveTimeout(10);
        template.setChannelTransacted(true);

        template.setReceiveTimeout(100);
        template.getUnconfirmed(100);
        template.setReplyTimeout(100);
        template.setExchange("download");
        template.setRoutingKey(queueName);
        template.setQueue(queueName);

        templateutil.setKey(conn.getKey());
        templateutil.setTemplate(template);
        return templateutil;
    }

}
