package com.xiangshangban.device.common.rmq;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.Connection;
import com.xiangshangban.device.common.utils.RabbitTemplateUtil;
import com.xiangshangban.device.service.impl.ConnectionFactoryServiceImpl;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * Created by liuguanglong on 2017/10/18.
 */

public class RabbitMQSender {

//    private static final Log LOG = LogFactory.getLog(RabbitMQSender.class);

    private static String SUCCESS = "success";
    private static String ERROR = "error";

//    @Value("${rabbitmq.download.exchange.name}")
//    private String downloadExchangeName;

    /**
     * 发送消息
     * @param queueName
     * @param message
     * @throws
     */
    public String sendMessage(String queueName,Object message) {

        //动态队列名称
        queueName = "device."+queueName+".download";

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
//            ConnectionFactoryServiceImpl.destoryConnection(templateutil.getKey());
            //使用完的连接放回空闲连接池
            ConnectionFactoryServiceImpl.connection.put(templateutil.getKey(),ConnectionFactoryServiceImpl.useConn.get(templateutil.getKey()));
            //从使用中连接池移除本次的连接
            ConnectionFactoryServiceImpl.useConn.remove(templateutil.getKey());

            System.out.println("已向【"+queueName+"】队列发送消息.....................................");
        }else{
            System.out.println("RABBITMQ'S ERROR: can not intalialize the template.");
            return RabbitMQSender.ERROR;
        }
        return RabbitMQSender.SUCCESS;
    }

    /**
     * 获取发送模板
     * @param queueName
     * @return
     */
    private  RabbitTemplateUtil getRabbitMQTemplate(String queueName) {

//        String exchange = downloadExchangeName;

        String exchange = "download";

        Connection conn = ConnectionFactoryServiceImpl.getConnectionFactory();
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
//        System.out.println("------------"+exchange);
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
