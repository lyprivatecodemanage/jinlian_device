package com.xiangshangban.device.common.rmq;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.Connection;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.utils.RabbitTemplateUtil;
import com.xiangshangban.device.service.impl.ConnectionFactoryServiceImpl;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by liuguanglong on 2017/10/18.
 */

public class RabbitMQSender {

//    private static final Log LOG = LogFactory.getLog(RabbitMQSender.class);

    private static String SUCCESS = "success";
    private static String ERROR = "error";

//    @Value("${rabbitmq.download.exchange.name}")
//    private String downloadExchangeName;

    private ConnectionFactoryServiceImpl connectionFactoryImpl = new ConnectionFactoryServiceImpl();

    /**
     * 发送消息
     * @param queueName
     * @param message
     * @throws Exception
     */
    public String sendMessage(String queueName,Object message) {

        //动态队列名称
        queueName = "device."+queueName+".download";

        System.out.println("已向【"+queueName+"】队列发送消息.....................................");

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
        System.out.println(md5check);
        String md5 = "";
        try {
            md5 = MD5Util.encryptPassword(md5check, "XC9EO5GKOIVRMBQ2YE8X");
            System.out.println("md5值 = " + md5);
        } catch (Exception e) {
            System.out.println("数据MD5出错:" + e.getMessage());
        }

        protocolMap.put("MD5Check", md5);

        return protocolMap;
    }

//    //发送测试
//    public static void main(String [] s) throws InterruptedException {
//        RabbitMQSender sd = new RabbitMQSender();
//        Map map = new LinkedHashMap();
//        Map map1 = new LinkedHashMap();
//        Map map2 = new LinkedHashMap();
//        Map mapData = new LinkedHashMap();
//
//        map1.put("adminFlag","");
//        map1.put("birthday","");
//        map1.put("contractExpired","");
//        map1.put("userDeptId","9B8DBB627C514502A2F5CC73C1BC7CA8");
//        map1.put("userDeptName","销售部");
//        map1.put("entryTime","2017-8-28");
//        map1.put("userId","13DFF865799A42C785F33AAFDC2FDD2D");
//        map1.put("userName","张慧1");
//        map1.put("userCode","N68761");
//        map1.put("userPhone","13022167724");
//        map1.put("probationaryExpired","");
//
//        map.put("commandIndex","1");
//        map.put("commandMode","C");
//        map.put("commandTotal","1");
//        map.put("commandType","single");
//
//        mapData.put("userInfo", map1);
//
//        map.put("data",mapData);
//        map.put("deviceId","1");
//        map.put("fileEdition","v1.3");
//        map.put("outOfTime","2017-10-26 10:32");
//        map.put("sendTime","2017-10-23 10:32:45");
//        map.put("serverId","1");
//        map.put("status","0");
//
//        map2.put("ACTION","UPDATE_USER_INFO");
//        map2.put("ACTIONCode","2001");
//        map2.put("subCMDID","");
//        map2.put("superCMDID","");
//
//        map.put("command", map2);
//
//        String md5check = JSON.toJSONString(map);
//        String md5 = "";
//        try {
//            md5 = MD5Util.encryptPassword(md5check, "XC9EO5GKOIVRMBQ2YE8X");
//            System.out.println("encry" + md5);
//        } catch (Exception e) {
//            System.out.println("数据MD5出错:" + e.getMessage());
//        }
//
//        map.put("MD5Check", md5);
//
////        JSONObject jsonObject= JSONObject.fromObject("");
////        Map<String, Object> map3 = (Map<String, Object>)jsonObject;
////        String str = (String) map3.get("md5Check");
////        map3.remove("md5Check");
////        JSON.toJSONString(map3);
//
////        while (true){
//        sd.sendMessage("hello", map);
////            System.out.println("[send] hello,刘文志");
//        System.out.println(JSON.toJSONString(map));
////            Thread.sleep(5000);
////        }
//
//    }

}
