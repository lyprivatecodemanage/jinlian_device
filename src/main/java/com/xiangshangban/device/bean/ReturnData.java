/**
 * Copyright (C), 2015-2017, 上海金念有限公司
 * FileName: ReturnData
 * Author:   liuguanglong
 * Date:     2017/11/6 14:18
 * Description: 请求返回数据模型
 * History:
 * <author>          <time>          <version>          <desc>
 * 作者姓名           修改时间           版本号              描述
 */
package com.xiangshangban.device.bean;

/**
 * 〈一句话功能简述〉<br> 
 * 〈请求返回数据模型〉
 *
 * @author liuguanglong
 * @create 2017/11/6
 * @since 1.0.0
 */

public class ReturnData {

    private Object data;//数据
    private String message;//请求状态描述
    private String returnCode;//请求状态编码

    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getReturnCode() {
        return returnCode;
    }
    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

}
