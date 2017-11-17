package com.xiangshangban.device.service.impl;

import com.xiangshangban.device.bean.Connection;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.service.IConnectionFactoryService;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuguanglong on 2017/10/18.
 */

public class ConnectionFactoryServiceImpl implements IConnectionFactoryService {

//    private static final Log LOG = LogFactory.getLog(ConnectionFactoryServiceImpl.class);

    private static Map<String,CachingConnectionFactory> connection;//空闲的连接
    private static Map<String,CachingConnectionFactory> useConn; //使用中的连接
    private static String host = "192.168.0.111";
    private static String username = "test";
    private static String password = "123";
    private static int port = 5672;
    private static String virtualHost= "/";

    public ConnectionFactoryServiceImpl(){
        System.out.println("创建MQ连接");
        int i = 0;
        connection = new HashMap<String, CachingConnectionFactory>();
        useConn = new HashMap<String, CachingConnectionFactory>();
        while (i < 10) {
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.resetConnection();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setVirtualHost(virtualHost);
            connectionFactory.setRequestedHeartBeat(180);
            connectionFactory.setCloseTimeout(10);
            connection.put(FormatUtil.createUuid(), connectionFactory);
            i++;
        }
        System.out.println("创建MQ连接完成");
    }


    /**
     * 获取链接工厂
     * @return
     */
    public Connection getConnectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory() ;
        Connection conn = new Connection();
        for (Map.Entry<String,CachingConnectionFactory> entry : connection.entrySet()) {
            conn.setConnectionFactory(entry.getValue());
            conn.setKey(entry.getKey());
            useConn.put(entry.getKey(), entry.getValue());
            connection.remove(entry.getKey());
            break;
        }

        if(conn.getKey() == null){
            connectionFactory = new CachingConnectionFactory() ;
            connectionFactory.resetConnection();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setRequestedHeartBeat(180);
            connectionFactory.setCloseTimeout(10);
            conn.setConnectionFactory(connectionFactory);
            conn.setKey(FormatUtil.createUuid());
            useConn.put(conn.getKey(), connectionFactory);
        }
        return conn;
    }

    //释放连接
    public static void destoryConnection(String key){
        useConn.get(key).destroy();
        useConn.remove(key);
        if(key.length()<3){
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory() ;
            connectionFactory.resetConnection();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            connectionFactory.setRequestedHeartBeat(180);
            connectionFactory.setCloseTimeout(10);
            connection.put(FormatUtil.createUuid(), connectionFactory);
        }
    }

}
