package com.xiangshangban.device.common.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/26 11:29
 * describe: TODO
 */
public class PageUtils {

    public static Map doSplitPage(List<Map> mapList,Object page,Object rows){
        Map map = new HashMap();
        //判断是否查询出数据
        if(mapList!=null&&mapList.size()>0){

            map.put("data",mapList);
            map.put("returnCode","3000");
            map.put("message","数据请求成功");

            //如果进行分页，添加分页信息
            if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
                //获取数据的总行数
                int totalCount = mapList.size();
                //获取总页数
                int pageSize = Integer.parseInt(rows.toString());
                int totalPage = totalCount%pageSize==0?totalCount/pageSize:(totalCount/pageSize)+1;

                map.put("totalCount",String.valueOf(totalCount));
                map.put("totalPage",String.valueOf(totalPage));
            }


        }else{
            map.put("returnCode","4007");
            map.put("message","结果为null");
        }
        return map;
    }
}
