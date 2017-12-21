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

    public static Map<String, CachingConnectionFactory> connection;//空闲的连接
    public static Map<String, CachingConnectionFactory> useConn; //使用中的连接

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //本地环境
//    private static String host = "192.168.0.118";
    //测试环境
    private static String host = "192.168.0.242";
    private static String username = "test";
    private static String password = "123";
    //真实环境
//    private static String host = "106.14.63.175";
//    private static String username = "jinnianmq";
//    private static String password = "jinnian-123456";
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static int port = 5672;
    private static String virtualHost = "/";

    static {
        System.out.println("连接池初始化20个MQ连接");
        int i = 0;
        connection = new HashMap<String, CachingConnectionFactory>();
        useConn = new HashMap<String, CachingConnectionFactory>();
        //连接池最大连接数
        while (i < 20) {
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
        System.out.println("初始化20个MQ连接完成");
    }

    /**
     * 获取链接工厂
     *
     * @return
     */
    public static Connection getConnectionFactory() {

        //从空闲的连接里获取连接
        Connection conn = new Connection();
        for (Map.Entry<String, CachingConnectionFactory> entry : connection.entrySet()) {
            conn.setConnectionFactory(entry.getValue());
            conn.setKey(entry.getKey());
            useConn.put(entry.getKey(), entry.getValue());
            connection.remove(entry.getKey());
            break;
        }

        //如果没有空闲的连接了
        if (conn.getKey() == null) {
            System.out.println("没有空闲的连接了.............................................................");
        }
        return conn;
    }

//    //释放连接（废弃不用）
//    public static void destoryConnection(String key){
//        useConn.get(key).destroy();
//        useConn.remove(key);
//        if(key.length()<3){
//            CachingConnectionFactory connectionFactory = new CachingConnectionFactory() ;
//            connectionFactory.resetConnection();
//            connectionFactory.setHost(host);
//            connectionFactory.setPort(port);
//            connectionFactory.setUsername(username);
//            connectionFactory.setPassword(password);
//            connectionFactory.setRequestedHeartBeat(180);
//            connectionFactory.setCloseTimeout(10);
//            connection.put(FormatUtil.createUuid(), connectionFactory);
//        }
//    }

}
