package com.xiangshangban.bean;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.io.Serializable;

/**
 * Created by liuguanglong on 2017/10/18.
 */
public class ConnectionUtil implements Serializable {

    private static final long serialVersionUID = 1164785479321213848L;

    private CachingConnectionFactory connectionFactory;
    private String key;

    public CachingConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
    public void setConnectionFactory(CachingConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }

}
