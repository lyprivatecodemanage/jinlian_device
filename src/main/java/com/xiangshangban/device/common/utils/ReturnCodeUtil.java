package com.xiangshangban.device.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/11/13 15:09
 * describe: TODO
 */
public class ReturnCodeUtil {
    //TODO 添加响应码（针对删除、添加、更改等操作）
    public static Map addReturnCode(boolean result){
        Map map = new HashMap();
        if(result){
            map.put("returnCode","3000");
            map.put("message","操作成功");
        }else{
            map.put("returnCode","3001");
            map.put("message","操作失败");
        }
        return map;
    }
}
