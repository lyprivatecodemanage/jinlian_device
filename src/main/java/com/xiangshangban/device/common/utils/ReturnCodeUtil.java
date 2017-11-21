package com.xiangshangban.device.common.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/11/13 15:09
 * describe: TODO  添加响应码（针对删除、添加、更改、不进行分页的查询等操作）
 */
public class ReturnCodeUtil {
    /**
     * 针对删除、添加、更改等操作
     * @param result
     * @return
     */
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

    /**
     * 针对不进行分页的查询
     * @param object
     * @return
     */
    public static Map addReturnCode(Object object){
        Map map = new HashMap();
        if(object!=null){
            map.put("data",object);
            map.put("returnCode","3000");
            map.put("message","数据请求成功");
        }else{
            map.put("returnCode","4203");
            map.put("message","请求数据不存在");
        }
        return map;
    }

    /**
     * 针对其它（参数不完整等）
     * @Param flag 1：表示参数不完整
     * @return
     */
    public static Map addReturnCode(int flag){
        Map map = new HashMap();
        switch (flag){
            case 1:
                map.put("returnCode","3007");
                map.put("message","参数格式错误");
                break;
            case 2:
                break;
                default:
                    break;
        }
        return map;
    }
}
