package com.xiangshangban.common.rmq;

import com.xiangshangban.bean.MQMessage;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

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

}
