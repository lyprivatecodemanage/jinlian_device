package com.xiangshangban.device.common.utils;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/26 11:29
 * describe: TODO
 */
public class PageUtils {

    public static List<Map> doSplitPage(List<Map> mapList){

        //获取数据的总行数
        int totalCount = mapList.size();
        Map<String,Integer> map = new HashMap<String,Integer>();
        map.put("totalCount",totalCount);

        mapList.add(map);

        return mapList;
    }
}
