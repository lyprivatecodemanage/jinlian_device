package com.xiangshangban.device.common.utils;

import com.github.pagehelper.Page;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/26 11:29
 * describe: TODO 封装完整的数据格式 ：添加返回码、总页数、总行数信息
 */
public class PageUtils {

    /**
     *
     * @param oldMapList （返回给前端的分页前的数据）当一个业务中，需要进行多次查询的时候，
     *                   需要对查询出来数据进行组拼，这个时候使用PageHelper没有意义，所以为了获取分页前的总行数信息，需要使用分页前的数据
     * @param mapList （返回给前端的分页后的数据）（针对的是进行分页后）
     * @param page 当前页码
     * @param rows 每一页要显示的行数
     * @param pageObj PageHelper.startPage()方法返回值：Page对象，该对象包含数据的总行数
     * @return
     */
    public static Map doSplitPage(List<Map> oldMapList,List<Map> mapList, Object page, Object rows, Page pageObj){
        Map map = new HashMap();
        //判断是否查询出数据
        if(mapList!=null){

            if(mapList.size()==0){
                map.put("data",oldMapList);//未分页
            }else{
                map.put("data",mapList);//分页
            }

            map.put("returnCode","3000");
            map.put("message","数据请求成功");

            //如果进行分页，添加分页信息
            if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
                //获取数据的总行数
                Long totalCount = null;
                if(oldMapList!=null){
                    totalCount = ((Integer)oldMapList.size()).longValue();
                }
                if(pageObj!=null){
                    totalCount = pageObj.getTotal();
                }
                //获取总页数
                int pageSize = Integer.parseInt(rows.toString());
                int totalPage = totalCount.intValue()%pageSize==0?totalCount.intValue()/pageSize:(totalCount.intValue()/pageSize)+1;

                map.put("totalPages",String.valueOf(totalCount));//总条数
                map.put("pagecountNum",String.valueOf(totalPage));//总页数
            }

        }else{
            map.put("returnCode","4203");
            map.put("message","请求数据不存在");
        }
        return map;
    }
}
