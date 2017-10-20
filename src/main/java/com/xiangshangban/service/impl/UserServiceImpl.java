package com.xiangshangban.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.bean.Command;
import com.xiangshangban.service.IUserService;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**d
 * date: 2017/10/19 10:38
 * describe: TODO 用户管理实现类
 */

@Service
public class UserServiceImpl implements IUserService{

    //人员模块命令生成器
    @Override
    public void userCommandGenerate(String action, List<String> userIdCollection) {

        if (action.equals("UPDATE_USER_INFO")){

            Command command = new Command();

            command.setServerId("null");
            command.setDeviceId("");


        }else if (action.equals("DELETE_USER_INFO")){

        }
    }

    /**
     * 请求
     * @param sendurl
     * @param data
     * @return
     */
    public static String sendRequet(String sendurl, Object data) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(sendurl);
        StringEntity myEntity = new StringEntity( JSON.toJSONString(data,false),
                ContentType.APPLICATION_JSON);// 构造请求数据
        post.setEntity(myEntity);// 设置请求体
        String responseContent = null; // 响应内容
        CloseableHttpResponse response = null;
        try {
            response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                responseContent = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (response != null)
                    response.close();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (client != null)
                        client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseContent;
    }
}
