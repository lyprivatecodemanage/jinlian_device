package com.xiangshangban.device.listener;

import com.xiangshangban.device.common.rmq.RabbitMQReciever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by liuguanglong on 2017/10/27.
 */

//springboot监听器，监听rabbitmq
@Component
public class RabbitMQRecieverListener implements ApplicationListener<ApplicationReadyEvent>{

    @Autowired
    private RabbitMQReciever rabbitMQReciever;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
      /*try {

            rabbitMQReciever.startRabbitMqReceiver();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }*/
    }
}
