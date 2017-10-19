package com.xiangshangban.common.utils;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.Serializable;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class RabbitTemplateUtil implements Serializable {

    private static final long serialVersionUID = 6980390931309342574L;
    private RabbitTemplate template ;
    private String key;

    public RabbitTemplate getTemplate() {
        return template;
    }
    public void setTemplate(RabbitTemplate template) {
        this.template = template;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

}
