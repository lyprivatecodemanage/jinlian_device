package com.xiangshangban.common.rmq;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.bean.Connection;
import com.xiangshangban.common.encode.DESEncode;
import com.xiangshangban.common.utils.RabbitTemplateUtil;
import com.xiangshangban.service.impl.ConnectionFactoryServiceImpl;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class RabbitMQSender {

//    private static final Log LOG = LogFactory.getLog(RabbitMQSender.class);

    private static String SUCCESS = "success";
    private static String ERROR = "error";
    private ConnectionFactoryServiceImpl connectionFactoryImpl = new ConnectionFactoryServiceImpl();

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
            byte[] encry = null ;
            try {
                encry = DESEncode.encrypt(json).getBytes();
                System.out.println("encry" + encry.toString());
            } catch (Exception e) {
                System.out.println("数据加密出错:"+e.getMessage());
            }
            template.convertAndSend(encry);
            template.convertAndSend(json);
            ConnectionFactoryServiceImpl.destoryConnection(templateutil.getKey());
        }else{
            System.out.println("RABBITMQ'S ERROR: can not intalialize the template.");
            return RabbitMQSender.ERROR;
        }
        return RabbitMQSender.SUCCESS;
    }

    //发送测试
    public static void main(String [] s) throws InterruptedException {
        RabbitMQSender sd = new RabbitMQSender();
        while (true){
            sd.sendMessage("hello", "hello,刘文志");
            System.out.println("[send] hello,刘文志");
            Thread.sleep(5000);
        }

    }


    /**
     * 获取发送模板
     * @param queueName
     * @return
     */
    private  RabbitTemplateUtil getRabbitMQTemplate(String queueName) {

        String exchange = "download";

        Connection conn = connectionFactoryImpl.getConnectionFactory();
        ConnectionFactory connectionFactory = conn.getConnectionFactory();

        if( null == connectionFactory) {
            return null;
        }

        RabbitTemplateUtil templateutil = new RabbitTemplateUtil();
        RabbitTemplate template = new RabbitTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setReceiveTimeout(10);
        template.setChannelTransacted(true);
        template.setReceiveTimeout(100);
        template.getUnconfirmed(100);
        template.setReplyTimeout(100);
        template.setExchange(exchange);
        template.setRoutingKey(queueName);
        template.setQueue(queueName);

//        //绑定交换器和队列根据routingKey
//        BindingBuilder.bind(new Queue("queueName", true)).to(new TopicExchange(exchange)).with(queueName);

        templateutil.setKey(conn.getKey());
        templateutil.setTemplate(template);
        return templateutil;
    }

}
